package helper;

import bean.BaseBean;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.*;
import org.elasticsearch.spark.rdd.api.java.JavaEsSpark;
import util.GsonUtil;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 数据处理
 */
public class DataHandler implements Serializable {
    private volatile static DataHandler instance;
    private SparkSession spark;
    // 数据缓存
    public JavaRDD<String> rddCache;

    private DataHandler() {
        this.spark = SparkSession.builder().master("local[2]").appName("handler").getOrCreate();
        this.spark.sparkContext().setLogLevel("WARN");
    }

    public static DataHandler getInstance() {
        if (instance == null) {
            synchronized (DataHandler.class) {
                if (instance == null) {
                    instance = new DataHandler();
                }
            }
        }
        return instance;
    }

    public Dataset<Row> handlerData(String filePath,boolean cache) {
        final JavaSparkContext ctx = JavaSparkContext.fromSparkContext(this.spark.sparkContext());
        JavaRDD<String> rdd = this.spark.read().textFile(filePath).javaRDD();
        // 表示数据缓存
        if(cache){
            rddCache = rdd.cache();
        }
        JavaRDD<BaseBean> rowRDD = rdd.map(new Function<String, BaseBean>() {
            public BaseBean call(String jsonStr) throws Exception {
                BaseBean baseBean = GsonUtil.parseJsonWithGson(jsonStr, BaseBean.class);
                Segment segment = Segment.getInstance();
                List<String> wordList = segment.nlp(baseBean.getBody());
                baseBean.setWords(wordList);
                return baseBean;
            }
        });

        JavaRDD<BaseBean> filterBeanRDD = rowRDD.filter(bean -> bean.getWords().size() >= 3 || !bean.getBody().contains("加入本群"));

        return this.spark.createDataFrame(filterBeanRDD, BaseBean.class);
    }

    /**
     * 将单条数据变成DataSet
     *
     * @param baseBean
     * @return
     */
    public Dataset<BaseBean> handlerOneData(BaseBean baseBean) {
        final JavaSparkContext ctx = JavaSparkContext.fromSparkContext(this.spark.sparkContext());
        Encoder<BaseBean> beanEncoder = Encoders.bean(BaseBean.class);
        return this.spark.createDataset(
                Collections.singletonList(baseBean),
                beanEncoder
        );
    }

    /**
     * 废弃,在计算IDF的时候,有异常
     * @param index
     * @return
     */
    public Dataset<Row> getDataByES(String index){
        final JavaSparkContext ctx = JavaSparkContext.fromSparkContext(this.spark.sparkContext());
        JavaRDD<Map<String, Object>> esRDD = JavaEsSpark.esRDD(ctx, index).values();
        JavaRDD<BaseBean> beanRDD = esRDD.map(new Function<Map<String, Object>, BaseBean>() {
            @Override
            public BaseBean call(Map<String, Object> v1) throws Exception {
                String body = (String) v1.get("body");
                BaseBean baseBean = new BaseBean();
                baseBean.setBody(body);
                Segment segment = Segment.getInstance();
                List<String> wordList = segment.nlp(body);
                baseBean.setWords(wordList);
                return baseBean;
            }
        });
        JavaRDD<BaseBean> beanFilterRDD = beanRDD.filter(bean -> bean.getWords().size() >= 3 || !bean.getBody().contains("加入本群"));
        JavaRDD<BaseBean> cache = beanFilterRDD.cache();

        return this.spark.createDataFrame(cache, BaseBean.class);
    }
}
