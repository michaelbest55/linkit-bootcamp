
// This code is meant to upload files to the HDFS
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import scala.io.Source

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
  
  //Hdfs.write("hdfs://sandbox-hdp.hortonworks.com:8020", "hdfs://sandbox-hdp.hortonworks.com:8020/user/anonymous/drivers.csv", "src/main/resources/data-spark/drivers.csv")
  //Hdfs.write("hdfs://sandbox-hdp.hortonworks.com:8020", "hdfs://sandbox-hdp.hortonworks.com:8020/user/anonymous/timesheet.csv", "src/main/resources/data-spark/timesheet.csv")
  //Hdfs.write("hdfs://sandbox-hdp.hortonworks.com:8020", "hdfs://sandbox-hdp.hortonworks.com:8020/user/anonymous/truck_event_text_partition.csv", "src/main/resources/data-spark/truck_event_text_partition.csv")   

  //val spark = org.apache.spark.sql.SparkSession.builder.master("spark://sandbox-hdp.hortonworks.com:4040").appName("Spark CSV Reader").getOrCreate;
  val spark = org.apache.spark.sql.SparkSession.builder.master("yarn").deployMode("client").appName("Spark CSV Reader").getOrCreate;
  //val df = spark.read.format("csv").option("header", "true").option("mode", "DROPMALFORMED").load("src/main/resources/data-spark/drivers.csv")

}
