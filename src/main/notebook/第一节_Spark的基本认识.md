## Spark介绍

### 什么是Spark

一个基于内存计算的开源计算系统,主要是基于MapReduce算法实现分布式计算

#### Spark Core

是一个基本引擎,用于大规模并行和分布式数据处理

RDD(Resulient Distributed Dataset): 弹性分布式数据集

    RDD是一个不可变,容错的,分布式对象集合,可以并行的操作这个集合,同时RDD提供了丰富的数据接口
    
### MLlib认识

由一系列机器学习算法和实用程序组成,包括分类,回归,聚类,协同过滤,降维,还包括一些底层的优化方法

#### 依赖

底层实现采用数值计算库Breeze和基础线性代数库BLAS

#### 优化计算

支持随机梯度下降法,少内存拟牛顿法,最小二乘法等

#### 回归

支持线性回归,岭回归,保序回归和以及与之相关的L1和L2正则化的变体,回归算法中采用的优化计算的是随机
梯度下降

#### 分类

支持贝叶斯分类,决策树分类,线性SVM和逻辑回归以及与之相关的L1和L2正则化的变体,采用的优化计算的是随
机梯度下降

#### 聚类

支持KMeans聚类算法,LDA主题模型算法

#### 推荐

支持ALS推荐,采用交替最小二乘求解的协同推荐算法

#### 关联规则

支持FPGrowth关联规则挖掘算法

### RDD操作

转换操作: map,flatMap,filter等

行动操作: count,saveAsTextFile,reduceByKey等

#### 创建

代码:

    // 通过数据集来创建
    val data = Array(1,2,3,4,5,6,7,8,9)
    // 第一个参数是数据集合,第二个参数指定数据分区
    val distData = sc.parallelize(data,3)
    // 内部数据创建
    distData.foreach(println)

对于参数将数据集划分成分片的数量,对每一个分片,Spark会在集群中运行一个对应的任务,
典型的情况下,集群中的每一个CPU将对应运行2-4个分片,一般情况Spark会根据当前集群的
情况自行设定分片数量,但是我们也可以将第二个参数传递给parallelize来手动确定分片数
量

也可以通过textFile来获取外部数据,来创建RDD对象,如:

    val distFile = sc.textFile("data.txt")
    
#### 转换操作

map: 对RDD中的每个元素执行指定一个函数的来产生一个新的RDD,RDD之间的元素是一对一关系

    如:
        scala> val rdd1 = sc.parallelize(1 to 9,3)
        rdd1: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[0] at parallelize at <console>:24
        scala> val rdd2 = rdd1.map(x => x*2)
        rdd2: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[1] at map at <console>:25
        scala> rdd2.collect()
        res0: Array[Int] = Array(2, 4, 6, 8, 10, 12, 14, 16, 18)
        
filter: 对RDD元素进行过滤

    如: 
        scala> val rdd3 = rdd2.filter(x => x > 10)
        rdd3: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[2] at filter at <console>:25
        scala> rdd3.collect()
        res1: Array[Int] = Array(12, 14, 16, 18)
        
flatMap: 每输入一个元素,会被映射为0到多个输出元素,因此,func函数返回的是一个seq,而不是
单一个元素,RDD之间是一对多关系

    如:
        scala> val rdd4 = rdd3.flatMap(x => x to 20)
        rdd4: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[3] at flatMap at <console>:25
        
        scala> rdd4.collect()
        res2: Array[Int] = Array(12, 13, 14, 15, 16, 17, 18, 19, 20, 14, 15, 16, 17, 18, 19, 20, 16, 17, 18, 19, 20, 18, 19, 20)
    解释一下: 以上示例的意思是,当x是12时,生成的12到20的序列,以此类推
    
mapPartitions: 输入的函数是是每个分区的数据,也就是把每个分区中的内容作为一个整体来处理

    def mapPartitions[U: ClassTag](f: Iterator[T] => Iterator[U],preservesPartitioning: Boolean = false): RDD[U]
    f: 输入函数,它处理的是每个分区中的内容,每个分区中的内容将会以Iterator[T]输入函数f,输出的是Iterator[U],最终的RDD
    由所有的分区经过输入函数处理之后结果合并起来
    
    如:
        scala> def myfunc[T](iter:Iterator[T]): Iterator[(T,T)] = {
             |   var res = List[(T,T)]()
             |   var pre = iter.next
             |   while(iter.hasNext){
             |     val cur = iter.next
             |     res.::=(pre,cur)
             |     pre = cur
             |   }
             |   res.iterator
             | }
        myfunc: [T](iter: Iterator[T])Iterator[(T, T)]
        
        scala> val rdd5 = rdd1.mapPartitions(myfunc)
        rdd5: org.apache.spark.rdd.RDD[(Int, Int)] = MapPartitionsRDD[4] at mapPartitions at <console>:27
        
        scala> rdd5.collect()
        res3: Array[(Int, Int)] = Array((2,3), (1,2), (5,6), (4,5), (8,9), (7,8))
    当map里面有比较耗时的操作时,比如断开和连接数据库,这个时候就可以采用mapPartitions,它只需要对每一个partition
    操作一次即可,函数的输入和输出都是iterator
    
sample: 对数据进行采样

    def sample(withReplacement: Boolean,fraction: Double,seed: Long = Utils.random.nextLong): RDD[T]
    根据随机给定的种子seed,随机抽样数量为fraction的数据,其中:
        withReplacement: 是否放回抽样
        fraction: 比例,比如0.1表示10%
        seed: 随机种子
        
    如:
        scala> val a = sc.parallelize(1 to 10000,3)
        a: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[5] at parallelize at <console>:24
        
        scala> a.sample(false,0.1,0).count()
        res14: Long = 1032
        
union: 数据合并,返回一个新的数据集

    如:
        scala> val rdd8 = rdd1.union(rdd3)
        rdd8: org.apache.spark.rdd.RDD[Int] = UnionRDD[7] at union at <console>:27
        
        scala> rdd8.collect()
        res15: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 14, 16, 18)
        
intersection: 数据交集

    如:
        scala> val rdd9 = rdd8.intersection(rdd1)
        rdd9: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[13] at intersection at <console>:27
        
        scala> rdd9.collect()
        res17: Array[Int] = Array(6, 1, 7, 8, 2, 3, 9, 4, 5)

distinct: 数据去重

    如:
        scala> val rdd10 = rdd8.union(rdd9).distinct
        rdd10: org.apache.spark.rdd.RDD[Int] = MapPartitionsRDD[17] at distinct at <console>:27
        
        scala> rdd10.collect()
        res18: Array[Int] = Array(12, 1, 14, 2, 3, 4, 16, 5, 6, 18, 7, 8, 9)
        
groupByKey: 数据分组操作,在一个由(K,V)对组成的数据集上调用,返回一个(K,Seq[V])对的数据集,默认情况下
使用8个并行任务进行分组,也可以传入numTask可选参数,根据数据量设置不同数目的Task

    如: 
        scala> val rdd0 = sc.parallelize(Array((1,1),(1,2),(1,3),(2,1),(2,2),(2,3)),3)
        rdd0: org.apache.spark.rdd.RDD[(Int, Int)] = ParallelCollectionRDD[18] at parallelize at <console>:24
        
        scala> val rdd11 = rdd0.groupByKey()
        rdd11: org.apache.spark.rdd.RDD[(Int, Iterable[Int])] = ShuffledRDD[19] at groupByKey at <console>:25
        
        scala> rdd11.collect()
        res19: Array[(Int, Iterable[Int])] = Array((1,CompactBuffer(1, 2, 3)), (2,CompactBuffer(1, 2, 3)))
        
reduceByKey: 数据分组聚合操作,在一个由(K,V)对组成的数据集上使用,返回一个(K,V)对的数据集,key相同的值,都被使
用reduce函数聚合到一起,任务个数也可以通过numTasks来配置

    如:
        scala> val rdd12 = rdd0.reduceByKey((x,y) => x +y)
        rdd12: org.apache.spark.rdd.RDD[(Int, Int)] = ShuffledRDD[20] at reduceByKey at <console>:25
        
        scala> rdd12.collect()
        res20: Array[(Int, Int)] = Array((1,6), (2,6))
        
aggregateByKey: 

    aggregateByKey(zeroValue:U)(seqOp:(U,T)=>U,combOp:(U,V)=>U)和reduceByKey的不同在于,reduceByKey输入/输出
    都是(K,V),而aggregateByKey输出是(K,U),可以不同于输入(K,V)
        参数说明:
            zeroValue:U, 初始值,比如空列表
            seqOp:(U,T)=>U, seq操作符,描述如何将T合并入U,比如如何将item合并到列表
            combOp:(U,U)=>U, comb操作符,描述如何合并两个U,比如合并两个列表
            
    如:
    
        scala> rdd0.collect()
        res31: Array[(Int, Int)] = Array((1,1), (1,2), (1,3), (2,1), (2,2), (2,3))
        
        scala> val z = rdd0.aggregateByKey(0)(math.max(_,_),_ + _)
        res28: org.apache.spark.rdd.RDD[(Int, Int)] = ShuffledRDD[23] at aggregateByKey at <console>:26
        
        scala> z.collect()
        res29: Array[(Int, Int)] = Array((1,5), (2,4))
      
sortByKey: 排序操作

    sortByKey([ascending],[numTasks]),对(K,V)类型的数据按照K进行排序,其中K要实现Orderd方法
    
    如: 
        scala> rdd0.collect()
        res33: Array[(Int, Int)] = Array((1,1), (1,2), (1,3), (2,1), (2,2), (2,3))
        
        scala> val rdd14 = rdd0.sortByKey()
        rdd14: org.apache.spark.rdd.RDD[(Int, Int)] = ShuffledRDD[30] at sortByKey at <console>:25
        
        scala> rdd14.collect()
        res34: Array[(Int, Int)] = Array((1,1), (1,2), (1,3), (2,1), (2,2), (2,3))
        
join: 连接操作,将输入数据集(K,V)和另外一个数据集(K,W)进行join,得到(K,(V,W)),V和W进行的笛卡尔积操作

    如:
        scala> val rdd15 = rdd0.join(rdd0)
        rdd15: org.apache.spark.rdd.RDD[(Int, (Int, Int))] = MapPartitionsRDD[33] at join at <console>:25
        
        scala> rdd15.collect()
        res35: Array[(Int, (Int, Int))] = Array((1,(1,1)), (1,(1,2)), (1,(1,3)), (1,(2,1)), (1,(2,2)), (1,(2,3)), (1,(3,1)), (1,(3,2)), (1,(3,3)), (2,(1,1)), (2,(1,2)), (2,(1,3)), (2,(2,1)), (2,(2,2)), (2,(2,3)), (2,(3,1)), (2,(3,2)), (2,(3,3)))

    还有左连接,右连接,全连接操作: leftOuterJoin,rightOuterJoin,fullOuterJoin
    
cogroup: 将输入数据集(K,V)和另外一个数据集(K,W)进行cogroup,得到一个格式为(K,Seq[V],Seq[W])的数据集

    如:
        scala> val rdd16 = rdd0.cogroup(rdd0)
        rdd16: org.apache.spark.rdd.RDD[(Int, (Iterable[Int], Iterable[Int]))] = MapPartitionsRDD[35] at cogroup at <console>:25
        
        scala> rdd16.collect()
        res36: Array[(Int, (Iterable[Int], Iterable[Int]))] = Array((1,(CompactBuffer(1, 2, 3),CompactBuffer(1, 2, 3))), (2,(CompactBuffer(1, 2, 3),CompactBuffer(1, 2, 3))))
        
cartesian: 做笛卡尔积操作,对于数据集T和U进行笛卡尔积操作,得到(T,U)格式的数据集

    如:
        scala> rdd1.collect()
        res37: Array[Int] = Array(1, 2, 3, 4, 5, 6, 7, 8, 9)
        
        scala> rdd3.collect()
        res38: Array[Int] = Array(12, 14, 16, 18)
        
        scala> val rdd17 = rdd1.cartesian(rdd3)
        rdd17: org.apache.spark.rdd.RDD[(Int, Int)] = CartesianRDD[36] at cartesian at <console>:27
        
        scala> rdd17.collect()
        res39: Array[(Int, Int)] = Array((1,12), (2,12), (3,12), (1,14), (1,16), (1,18), (2,14), (2,16), (2,18), (3,14), (3,16), (3,18), (4,12), (5,12), (6,12), (4,14), (4,16), (4,18), (5,14), (5,16), (5,18), (6,14), (6,16), (6,18), (7,12), (8,12), (9,12), (7,14), (7,16), (7,18), (8,14), (8,16), (8,18), (9,14), (9,16), (9,18))
        
#### 行动操作

reduce: 对数据集的所有元素执行聚集(func)函数,该函数必须是可交换的

    如:
        scala> val rdd1 = sc.parallelize(1 to 9,3)
        rdd1: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[38] at parallelize at <console>:24
        
        scala> val rdd2 = rdd1.reduce(_ + _)
        rdd2: Int = 45
        
collect: 将数据集中的所有元素以一个Array的形式返回

count: 返回数据集中元素的个数

first: 获取数据集中的第一个元素

take: 返回前n个元素的数组

saveAsTextFile: 把数据集中的元素写到一个文本文件中

foreach: 对数据集中的每个元素都执行func函数

### 统计操作

MLlib Statistics是基础统计模块,对RDD格式数据进行统计,包括: 汇总统计,相关系数,分层抽样,假设检验,
随机数据生成等

#### 列统计汇总

colStats: 计算每列的最大值,最小值,平均值,方差值,L1范数,L2范数

    如:
        scala> val data_path = "D:\\java_workplace\\sparkMLlib\\src\\data\\sample_stat.txt"
        data_path: String = D:\java_workplace\sparkMLlib\src\data\sample_stat.txt
        
        scala> val data = sc.textFile(data_path).map(_.split("\t")).map(f => f.map( f => f.toDouble))
        data: org.apache.spark.rdd.RDD[Array[Double]] = MapPartitionsRDD[42] at map at <console>:26
        
        scala> data.collect()
        res40: Array[Array[Double]] = Array(Array(1.0, 2.0, 3.0, 4.0, 5.0), Array(6.0, 7.0, 1.0, 5.0, 9.0), Array(3.0, 5.0, 6.0, 3.0, 1.0), Array(3.0, 1.0, 1.0, 5.0, 6.0))
        
        将数据转换为RDD[Vector]类型
        scala> import org.apache.spark.mllib.linalg._
        import org.apache.spark.mllib.linalg._
        
        scala> val data1 = data.map(f => Vectors.dense(f))
        data1: org.apache.spark.rdd.RDD[org.apache.spark.mllib.linalg.Vector] = MapPartitionsRDD[43] at map at <console>:28
        
        scala> data1.collect()
        res44: Array[org.apache.spark.mllib.linalg.Vector] = Array([1.0,2.0,3.0,4.0,5.0], [6.0,7.0,1.0,5.0,9.0], [3.0,5.0,6.0,3.0,1.0], [3.0,1.0,1.0,5.0,6.0])
        分别求最大值,最小值,平均值,方差值,L1,L2
        scala> import org.apache.spark.mllib.stat._
        import org.apache.spark.mllib.stat._
        
        scala> val stat1 = Statistics.colStats(data1)
        stat1: org.apache.spark.mllib.stat.MultivariateStatisticalSummary = org.apache.spark.mllib.stat.MultivariateOnlineSummarizer@6bd668a6
        
        scala> stat1.max
        res46: org.apache.spark.mllib.linalg.Vector = [6.0,7.0,6.0,5.0,9.0]
        
        scala> stat1.min
        res47: org.apache.spark.mllib.linalg.Vector = [1.0,1.0,1.0,3.0,1.0]
        
        scala> stat1.mean
        res48: org.apache.spark.mllib.linalg.Vector = [3.25,3.75,2.75,4.25,5.25]
        
        scala> stat1.variance
        res49: org.apache.spark.mllib.linalg.Vector = [4.25,7.583333333333333,5.583333333333333,0.9166666666666666,10.916666666666666]
        
        scala> stat1.normL1
        res50: org.apache.spark.mllib.linalg.Vector = [13.0,15.0,11.0,17.0,21.0]
        
        scala> stat1.normL2
        res51: org.apache.spark.mllib.linalg.Vector = [7.416198487095663,8.888194417315589,6.855654600401044,8.660254037844387,11.958260743101398]

#### 相关系数

Pearson相关系数表达的是两个数值变量的线性相关性,它一般适用于正态分布,其取值范围是[-1,1],取值为0表示不相关,取值为(0,-1]
表示负相关,取值(0,1]表示正相关       
    
Spearman相关系数也用来表达两个变量的相关系,但是它没有Pearson相关系数的分布要求那么严格,它可以更好的用于测度变量的排序关系

    如:
        scala> val corr1 = Statistics.corr(data1,"pearson")
        2018-09-28 19:52:53 WARN  BLAS:61 - Failed to load implementation from: com.github.fommil.netlib.NativeSystemBLAS
        2018-09-28 19:52:53 WARN  BLAS:61 - Failed to load implementation from: com.github.fommil.netlib.NativeRefBLAS
        corr1: org.apache.spark.mllib.linalg.Matrix =
        1.0                   0.7779829610026362    -0.39346431156047523  ... (5 total)
        0.7779829610026362    1.0                   0.14087521363240252   ...
        -0.39346431156047523  0.14087521363240252   1.0                   ...
        0.4644203640128242    -0.09482093118615205  -0.9945577827230707   ...
        0.5750122832421579    0.19233705001984078   -0.9286374704669208   ...
        
        scala> val corr2 = Statistics.corr(data1,"spearman")
        corr2: org.apache.spark.mllib.linalg.Matrix =
        1.0                  0.632455532033675     -0.5000000000000001  ... (5 total)
        0.632455532033675    1.0                   0.10540925533894883  ...
        -0.5000000000000001  0.10540925533894883   1.0                  ...
        0.5000000000000001   -0.10540925533894883  -1.0000000000000002  ...
        0.6324555320336723   0.20000000000000429   -0.9486832980505085  ...
        
        对x,y求相关性
        scala> val x1 = sc.parallelize(Array(1.0,2.0,3.0,4.0))
        x1: org.apache.spark.rdd.RDD[Double] = ParallelCollectionRDD[58] at parallelize at <console>:30
        
        scala> val y1 = sc.parallelize(Array(5.0,6.0,6.0,6.0))
        y1: org.apache.spark.rdd.RDD[Double] = ParallelCollectionRDD[59] at parallelize at <console>:30
        
        scala> val corr3 = Statistics.corr(x1,y1,"pearson")
        corr3: Double = 0.7745966692414775
        
#### 假设检验   

MLlib支持用于判断拟合度或者独立的Pearson卡方检验,不同的输入类型决定了是做拟合度检验还是独立检验,拟合度检验要求
是Vector,独立检验要求输入是Matrix

卡方检验:

    如:
        scala> val v1 = Vectors.dense(43.0,9.0)
        v1: org.apache.spark.mllib.linalg.Vector = [43.0,9.0]
        
        scala> val v2 = Vectors.dense(44.0,4.0)
        v2: org.apache.spark.mllib.linalg.Vector = [44.0,4.0]
        
        scala> val c1 = Statistics.chiSqTest(v1,v2)
        c1: org.apache.spark.mllib.stat.test.ChiSqTestResult =
        Chi squared test summary:
        method: pearson
        degrees of freedom = 1
        statistic = 5.482517482517483
        pValue = 0.01920757707591003
        Strong presumption against null hypothesis: observed follows the same distribution as expected..

    统计量为pearson,自由度为1,值为5.48,概率为0.0192

## MLlib数据格式

### 数据处理

MLUtils用于辅助加载,保存,处理MLlib相关算法所需要的数据,其中主要的方法是loadLibSVMFile,用于加载LIBSVM
格式数据,返回RDD[LabeledPoint]格式数据,该数据格式可以用于分类,回归等算法中

loadLibSVMFile:

    /**
      * loadLibSVMFile
      * 加载LIBSVM格式数据,返回RDD[LabeledPoint]
      * LabeledPoint格式:
      *   (label: Double,features: Vector),label代表标签,features代表特征向量
      * 输入LIBSVM格式的数据,格式如下:
      * {{{label index1:value1 index2:value2 ...}}}
      * label代表标签,value代表特征,index代表特征位置索引
      */
      
    如:
        scala> import org.apache.spark.mllib.util._
        import org.apache.spark.mllib.util._
        
        scala> val data = MLUtils.loadLibSVMFile(sc,"D:\\java_workplace\\sparkMLlib\\src\\data\\sample_libsvm_data.txt")
        data: org.apache.spark.rdd.RDD[org.apache.spark.mllib.regression.LabeledPoint] = MapPartitionsRDD[6] at map at MLUtils.scala:86
        
        scala> data.take(1)
        res0: Array[org.apache.spark.mllib.regression.LabeledPoint] = Array((0.0,(692,[127,128,129,130,131,154,155,156,157,158,159,181,182,183,184,185,186,187,188,189,207,208,209,210,211,212,213,214,215,216,217,235,236,237,238,239,240,241,242,243,244,245,262,263,264,265,266,267,268,269,270,271,272,273,289,290,291,292,293,294,295,296,297,300,301,302,316,317,318,319,320,321,328,329,330,343,344,345,346,347,348,349,356,357,358,371,372,373,374,384,385,386,399,400,401,412,413,414,426,427,428,429,440,441,442,454,455,456,457,466,467,468,469,470,482,483,484,493,494,495,496,497,510,511,512,520,521,522,523,538,539,540,547,548,549,550,566,567,568,569,570,571,572,573,574,575,576,577,578,594,595,596,597,598,599,600,601,602,603,604,622,623,624,625,626,627,628,629,630,651,652,653,654,655,656,657],[51.0,159.0,2...
    
saveAsLibSVMFile: 将LIBSVM格式数据保存到指定文件中

appendBias: 对向量增加偏置项,用于回归和分类算法中

fastSquaredDistance:快速计算距离的方法

    主要用于K-means聚类算法中
    fastSquaredDistance会先计算一个精度, 有关的精度计算:
        precisionBound1 = 2.0 * EPSILON * sumSquaredNorm / (normDiff * normDiff + EPSILON)
    如果在精度满足条件的情况下,则欧氏距离: EuclideanDist = sumSquaredNorm - 2.0 * v1.dot(v2)
    如果精度不满足,则按原始距离公式进行计算: (a1 - a2) ^ 2 + (b1 - b2) ^ 2
    
generateKMeansRDD: 样本生成

    用于生成KMeans的训练数据,格式为RDD[Array[Double]],参数如下:
        sc: SparkContext
        numPoint: RDD中包括的数据量
        k: 聚类数
        d: 数据维度
        r: 初始中心分布的缩放因子
        numPartitions: RDD的分区数        
    
    示例: 随机生成40个样本,数据维度为三维,聚类中心为5
        
        scala> import org.apache.spark.mllib.util._
        import org.apache.spark.mllib.util._
        
        scala> val KMeansRDD = KMeansDataGenerator.generateKMeansRDD(sc,40,5,3,1.0,2)
        KMeansRDD: org.apache.spark.rdd.RDD[Array[Double]] = MapPartitionsRDD[1] at map at KMeansDataGenerator.scala:60
        
        scala> KMeansRDD.count()
        res0: Long = 40
        
        scala> KMeansRDD.take(5)
        res1: Array[Array[Double]] = Array(Array(2.2838106309461095, 1.8388158979655758, -1.8997332737817918), Array(-0.6536454069660477, 0.9840269254342955, 0.19763938858718594), Array(0.24415182644986977, -0.4593305783720648, 0.3286249752173309), Array(1.8793621718715983, 1.4433606519575122, -0.9420612755690412), Array(2.7663276890005077, -1.4673057796056233, 0.39691668230812227))

generateLinearRDD: 生成线性回归训练样本数据,格式为RDD[LabeledPoint]

    参数:
        sc: SparkContext
        nexamples: RDD中包括的数据量
        nfeature: 样本的特征数
        eps: Epsilon因子
        nparts: RDD分区数
        
    示例: 随机生成40个样本,数据维度为三维
        scala> val LinearRDD = LinearDataGenerator.generateLinearRDD(sc,40,3,1.0,2,0.0)
        LinearRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.regression.LabeledPoint] = MapPartitionsRDD[3] at flatMap at LinearDataGenerator.scala:183
        
        scala> LinearRDD.count()
        res2: Long = 40
        
        scala> LinearRDD.take(5)
        res3: Array[org.apache.spark.mllib.regression.LabeledPoint] = Array((-0.9875339179115987,[0.4551273600657362,0.36644694351969087,-0.38256108933468047]), (1.135787192337867,[0.8067445293443565,-0.2624341731773887,-0.44850386111659524]), (-0.4741621894439617,[-0.07269284838169332,0.5658035575800715,0.8386555657374337]), (1.371466825495126,[-0.22686625128130267,-0.6452430441812433,0.18869982177936828]), (0.6347733840587794,[-0.5804648622673358,0.651931743775642,-0.6555641246242951]))

generateLogisticRDD: 生成逻辑回归训练样本数据

    参数:
        sc: SparkContext
        nexamples: RDD中包括的数据量
        nfeature: 样本的特征数
        eps: Epsilon因子
        nparts: RDD分区数
        probOne: 标签1的概率
        
    示例: 随机生成40个样本,数据维度为三维,标签1的概率为0.5
        scala> val LogisticRDD = LogisticRegressionDataGenerator.generateLogisticRDD(sc,40,3,1.0,2,0.5)
        LogisticRDD: org.apache.spark.rdd.RDD[org.apache.spark.mllib.regression.LabeledPoint] = MapPartitionsRDD[5] at map at LogisticRegressionDataGenerator.scala:55
        
        scala> LogisticRDD.count()
        res4: Long = 40
        
        scala> LogisticRDD.take(5)
        res5: Array[org.apache.spark.mllib.regression.LabeledPoint] = Array((0.0,[1.1419053154730547,0.9194079489827879,-0.9498666368908959]), (1.0,[1.4533448794332902,1.703049287361516,0.5130165929545305]), (0.0,[1.0613732338485966,0.9373128243059786,0.519569488288206]), (1.0,[1.3931487794809478,1.6410535022701498,0.17945164909645228]), (0.0,[1.3558214650566454,-0.8270729973920494,1.6065611415614136]))

#### 其他数据生成

    1) SVM: SVMGenerate
            参数说明:
            master: 集群Master地址
            output_dir: 样本输出路径
            num_examples: 样本数量
            num_features: 样本特征数量
            num_partition: 样本RDD分区数
            
    2) MFD: MFDataGenerate
            参数说明:
            master: 集群Master地址
            output_dir: 样本输出路径
            m: 矩阵行数
            n: 矩阵列数
            rank: 矩阵秩
            trainSampFact: 采样因子
            noise: 是否增加噪音
            sigma: 高斯噪音标准差
            test: 是否测试
            testSampFact: 测试比列


