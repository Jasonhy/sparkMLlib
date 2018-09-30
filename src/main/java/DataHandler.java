import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;

import java.io.Serializable;
import java.util.List;

/**
 * 数据处理
 */
public class DataHandler implements Serializable {
    private volatile static DataHandler instance;

    private DataHandler(){

    }
    public static DataHandler getInstance(){
        if(instance == null){
            synchronized (DataHandler.class){
                if(instance == null){
                    instance = new DataHandler();
                }
            }
        }
        return instance;
    }

    public void handlerData(String filePath){
        SparkConf conf = new SparkConf().setAppName("handler").setMaster("local[2]");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> rdd = sc.textFile(filePath);
        JavaRDD<DataBean> dataRdd = rdd.map(new Function<String, DataBean>() {
            public DataBean call(String jsonStr) throws Exception {
                DataBean dataBean = GsonUtils.parseJsonWithGson(jsonStr, DataBean.class);
                Segment segment = Segment.getInstance();
                List<String> wordList = segment.nlp(dataBean.getBody());
                dataBean.setWords(wordList);
                return dataBean;
            }
        });
        System.out.println(dataRdd.take(1));
    }
}
