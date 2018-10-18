package rdd
import org.apache.spark.{SparkConf,SparkContext}

/**
  * RDD的相关操作
  */
object RDDOperater {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("rdd").setMaster("local[2]")
    val sc = new SparkContext(conf)
    // 通过数据集来创建
    val data = Array(1,2,3,4,5,6,7,8,9)
    // 第一个参数是数据集合,第二个参数指定数据分区
    // 对于参数将数据集划分成分片的数量,对每一个分片,Spark会在集群中运行
    // 一个对应的任务
    val distData = sc.parallelize(data,3)
    // 内部数据创建
    // distData.foreach(println)
    // map(sc)
    filter(sc)
  }

  def map(sc: SparkContext): Unit ={
    val rdd1 = sc.parallelize(1 to 9,3)
    val rdd2 = rdd1.map(x => x*2)
    rdd2.foreach(println)
  }

  def filter(sc:SparkContext): Unit ={
    val rdd1 = sc.parallelize(1 to 9,3)
    val rdd2 = rdd1.map(x => x*2)
    val rdd3 = rdd2.filter(x => x > 10)
    rdd3.foreach(println)

    

  }
}
