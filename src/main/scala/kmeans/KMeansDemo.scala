package kmeans
import org.apache.log4j.{Level,Logger}
import org.apache.spark.{SparkConf,SparkContext}
import org.apache.spark.mllib.clustering._
import org.apache.spark.mllib.linalg.Vectors
/**
  * 聚类算法实例
  */
object KMeansDemo {
  def main(args: Array[String]): Unit = {
    // 构建Spark对象
    val conf = new SparkConf().setAppName("KMeans").setMaster("local[2]")
    val sc = new SparkContext(conf)

    Logger.getRootLogger.setLevel(Level.WARN)

    // 读取样本数据
    val data = sc.textFile("D:\\java_workplace\\sparkMLlib\\src\\data\\kmeans_data.txt")
    // 对数据进行转换
    val parsedData = data.map(s => Vectors.dense(s.split(" ").map(_.toDouble))).cache()

    // 新建KMeans聚类模型
    val initMode = "k-means||"
    val numClusters = 2
    val numIterations = 20
    val model = new KMeans()
      .setInitializationMode(initMode)
      .setK(numClusters)
      .setMaxIterations(numIterations)
      .run(parsedData)

    // 误差计算
    val WSSSE = model.computeCost(parsedData)
    println("Within Set Sum of Squared Errors = " + WSSSE)

    // 保存模型
    val ModelPath = "D:\\java_workplace\\sparkMLlib\\src\\model\\kmeans_model"
    model.save(sc,ModelPath)
    val sameModel = KMeansModel.load(sc,ModelPath)
  }
}
