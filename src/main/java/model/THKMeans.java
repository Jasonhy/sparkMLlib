package model;

import bean.BaseBean;
import config.Config;
import helper.DataHandler;
import helper.SimHash;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.HashingTF;
import util.GsonUtil;

import java.io.Serializable;
import java.util.*;

/**
 * 聚类处理
 */
public class THKMeans implements Serializable {
    private DataHandler handler;
    // 设置模型存储路径
    private String modelPath;
    // KMeans 模型
    private KMeansModel model;
    // 训练数据
    private Dataset<Row> datasetCache;
    private int numFeatures = 10;

    public THKMeans(){
        this.handler = DataHandler.getInstance();
        // 根据操作系统,获取模型存储路径
        String os = System.getProperty("os.name");
        // 模型存储路径
        if (os.toLowerCase().startsWith("win")) {
            modelPath = "D:\\model";
        } else {
            modelPath = "/data/model/";
        }
    }

    // 1 首先调用train开始训练
    public void train(Config config,boolean cache){
        Dataset<Row> dataset = this.handler.handlerData(config.getTrainFilePath(),cache);
        datasetCache = getRowDataset(dataset);
        // 聚类
        KMeans kMeans = new KMeans().setK(Config.valK).setSeed(1L);
        // 对模型进行训练
        model = kMeans.fit(datasetCache);
    }
    // 2 再调用模型进行预测
    public boolean predict(BaseBean baseBean){
        Map<Integer, Iterable<Row>> collectAsMap = getKMeansResult();
//        // 预测新数据的分类
        Row row = getOneRowDataset(baseBean);
//        // 获取该条记录输入哪个分类
        int predict = model.predict((Vector) row.getAs(4));
        System.out.println("body: " + baseBean.getBody() + " words: " + baseBean.getWords() + " predict: " + predict);
        // 根据预测的结果,获取该分类下的所有数据
        Iterable<Row> rows = collectAsMap.get(predict);
        boolean tag = true;
        SimHash currHash = new SimHash(baseBean.getBody(),64);
        for (Iterator<Row> iter = rows.iterator(); iter.hasNext();) {
            Row next = iter.next();
            String body = next.getString(0);
            SimHash oriHash = new SimHash(body, 64);
            int d = currHash.hammingDistance(currHash.simHashVal, oriHash.simHashVal);
            if (d <= 7) {
                tag = false;
                break;
            } else {
                tag = true;
            }
        }
        // 是否将数据插入
        return tag;
    }

    /**
     * 将分好类的数据返回
     * @return
     */
    public Map<Integer, Iterable<Row>> getKMeansResult(){
        // 对模型对数据进行转换
        Dataset<Row> output = model.transform(datasetCache);
        // 将Dataset数据变为RDD,方便将对应的数据进行分组
        JavaRDD<Row> rowJavaRDD = output.toJavaRDD();
        // 按指定的类别进行分组 Integer: 每个对应的类别, Iterable<Row>: 该类别下对应的值
        return  rowJavaRDD.groupBy(new Function<Row, Integer>() {
            public Integer call(Row v1) throws Exception {
                return v1.getInt(5);
            }
        }).collectAsMap();
    }

    /**
     * 获取TF-IDF DataSet
     * 并对数据进行cache操作
     * @param dataset
     * @return
     */
    private Dataset<Row> getRowDataset(Dataset<Row> dataset) {
        // TF
        HashingTF tf = new HashingTF()
                .setInputCol("words")
                .setOutputCol("rawFeatures")
                .setNumFeatures(numFeatures);
        Dataset<Row> transform = tf.transform(dataset);

        // IDF
        IDF idf = new IDF().setInputCol("rawFeatures").setOutputCol("features");
        IDFModel idfModel = idf.fit(transform);
        return idfModel.transform(transform).cache();
    }

    private Row getOneRowDataset(BaseBean baseBean) {
        Dataset<BaseBean> oneData = this.handler.handlerOneData(baseBean);
        // TF
        HashingTF tf = new HashingTF()
                .setInputCol("words")
                .setOutputCol("rawFeatures")
                .setNumFeatures(numFeatures);
        Dataset<Row> transform = tf.transform(oneData);
        // IDF
        IDF idf = new IDF().setInputCol("rawFeatures").setOutputCol("features");
        IDFModel idfModel = idf.fit(transform);
        Dataset<Row> cache = idfModel.transform(transform).cache();
        // |bodys|    uid |                          words|          rawFeatures|            features|
        // [修正了,4130670479274854a95a8d3501f7cf4f,WrappedArray(修正),(10,[3],[1.0]),(10,[3],[0.8882872271994813]),197]
        // System.out.println("ONE ROW: " + cache.first());
        return cache.first();
    }

    /**
     * 从缓存中获取训练的缓存数据
     * @return 返回对应的json数据
     */
    public String getTrainDataByCache(String uid){
        JavaRDD<String> rddCache = this.handler.rddCache;
        JavaRDD<String> jsonBean = rddCache.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String jsonStr) throws Exception {
                BaseBean baseBean = GsonUtil.parseJsonWithGson(jsonStr, BaseBean.class);
                return baseBean.getUid().equals(uid);
            }
        });
        return jsonBean.first();
    }
}
