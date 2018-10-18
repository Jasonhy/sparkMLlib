package format

/**
  * loadLibSVMFile
  * 加载LIBSVM格式数据,返回RDD[LabeledPoint]
  * LabeledPoint格式:
  *   (label: Double,features: Vector),label代表标签,features代表特征向量
  * 输入LIBSVM格式的数据,格式如下:
  * {{{label index1:value1 index2:value2 ...}}}
  * label代表标签,value代表特征,index代表特征位置索引
  *
  * saveAsLibSVMFile
  * 将LIBSVM格式数据保存到指定文件中
  */
object DataOperate {

}
