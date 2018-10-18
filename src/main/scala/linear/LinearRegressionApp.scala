package linear

import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionWithSGD}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 线性回归模型
  * LinearRegressionWithSGD: 伴生对象
  * 包含train静态方法,用于训练线性回归模型
  */
object LinearRegressionApp {
  def main(args: Array[String]): Unit = {
    // 1 构建Spark对象
    val conf = new SparkConf().setAppName("LinearRegressionApp").setMaster("local[2]")
    val sc = new SparkContext(conf)
    // 设置日志级别
    Logger.getRootLogger.setLevel(Level.WARN)

    // 读取数据
    val data_path = "D:\\java_workplace\\sparkMLlib\\src\\data\\lpsa.data"
    val data = sc.textFile(data_path)
    val examples = data.map{ line => val parts = line.split(",")
      LabeledPoint(parts(0).toDouble,Vectors.dense(parts(1).split(" ").map(_.toDouble)))}.cache()

    val numExamples = examples.count()
    // println(numExamples)
    // 3 新建线性回归模型,并设置训练参数
    val numIterations = 100
    val stepSize = 1
    val miniBatchFraction = 1.0
    val model = LinearRegressionWithSGD.train(examples,numIterations,
      stepSize,miniBatchFraction)
    // println(model.weights)
    // println(model.intercept)

    // 4 对样本进行测试
    val predict = model.predict(examples.map(_.features))
    val predictAndLabel = predict.zip(examples.map(_.label))
    val print_predict = predictAndLabel.take(50)
    for(i <- 0 to print_predict.length -1){
      println(print_predict(i)._1 + "\t" + print_predict(i)._2)
    }

    // 计算误差
    val loss = predictAndLabel.map{
      case (p,1) =>
        val err = p -1
        err * err
    }.reduce(_ + _)

    val rmse = math.sqrt(loss / numExamples)
    println(s"Test RMSE = $rmse")

  }
}
