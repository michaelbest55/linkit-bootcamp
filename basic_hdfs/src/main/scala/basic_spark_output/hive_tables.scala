// This code is meant to upload files to the HDFS
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import scala.io.Source
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import java.io.File


object Hdfs extends App{

 def write(uri: String, filePath: String, source: String) = { 
   System.setProperty("HADOOP_USER_NAME", "hdfs")
    val path = new Path(filePath)
    val conf = new Configuration()
    conf.set("fs.defaultFS", uri)
    val fs = FileSystem.get(conf)
    val os = fs.create(path)
    val fp = Source.fromFile(source)
    for (line <- fp.getLines())
      os.write(line.getBytes)
    fp.close()
    fs.close()
  }
  
  // Write files to HFDS on the HDP sandbox
  //Hdfs.write("hdfs://sandbox-hdp.hortonworks.com:8020", "hdfs://sandbox-hdp.hortonworks.com:8020/user/anonymous/drivers.csv", "src/main/resources/data-spark/drivers.csv")
  //Hdfs.write("hdfs://sandbox-hdp.hortonworks.com:8020", "hdfs://sandbox-hdp.hortonworks.com:8020/user/anonymous/timesheet.csv", "src/main/resources/data-spark/timesheet.csv")
  //Hdfs.write("hdfs://sandbox-hdp.hortonworks.com:8020", "hdfs://sandbox-hdp.hortonworks.com:8020/user/anonymous/truck_event_text_partition.csv", "src/main/resources/data-spark/truck_event_text_partition.csv")   
  


  // Create Spark Session
  val warehouseLocation = new File("spark-warehouse").getAbsolutePath
  val spark = SparkSession.builder().appName("driver data").master("local").config("spark.sql.warehouse.dir", warehouseLocation).enableHiveSupport().getOrCreate()
  spark.sql("set spark.sql.caseSensitive=true")   

  // load csv files as Spark DataFrames 
  val df = spark.read.option("header",true).csv("hdfs:///user/anonymous/drivers.csv")
  val df2 = spark.sql("SELECT * FROM csv.`hdfs:///user/anonymous/timesheet.csv`")
  //val df2 = spark.read.option("header",true).csv("hdfs:///user/anonymous/timesheet.csv")
  //val df3 = spark.read.option("header","true").option("maxColumns", 25000).schema(customSchema).csv("hdfs:///user/anonymous/truck_event_text_partition.csv")
  //val df3 = spark.sql("SELECT * FROM csv.`hdfs:///user/anonymous/truck_event_text_partition.csv`")

  // Save the tables in Hive
  df.createOrReplaceTempView("driversTempTable")
  spark.sqlContext.sql("create table driversTable4 as select * from driversTempTable")
  df2.createOrReplaceTempView("timeSheetTempTable")
  spark.sqlContext.sql("create table timeSheetTable4 as select * from timeSheetTempTable")
  //df3.createOrReplaceTempView("truckeventTempTable")
  //spark.sqlContext.sql("create table truckEventTable as select * from truckEventTempTable")
  
  // Join hive tables
  spark.sql("SELECT timeSheetTable4.driverId, name, `hours-logged`, `miles-logged`  FROM timeSheetTable4 JOIN driversTable4 WHERE driversTable4.driverId=timeSheetTable4.driverId").show()
}

