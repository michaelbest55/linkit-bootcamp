
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
  
  // Write files to HFDS on the HDP sandbox
  //Hdfs.write("hdfs://sandbox-hdp.hortonworks.com:8020", "hdfs://sandbox-hdp.hortonworks.com:8020/user/anonymous/drivers.csv", "src/main/resources/data-spark/drivers.csv")
  //Hdfs.write("hdfs://sandbox-hdp.hortonworks.com:8020", "hdfs://sandbox-hdp.hortonworks.com:8020/user/anonymous/timesheet.csv", "src/main/resources/data-spark/timesheet.csv")
  //Hdfs.write("hdfs://sandbox-hdp.hortonworks.com:8020", "hdfs://sandbox-hdp.hortonworks.com:8020/user/anonymous/truck_event_text_partition.csv", "src/main/resources/data-spark/truck_event_text_partition.csv")   
  
 //Create Spark context
 /*TODO: Set the HADOOP_CONF_DIR and all the necessary files to connect to the environment running in the HDP sandbox 
   as seen in https://stackoverflow.com/a/38483127/11807319 :
   fs.defaultFS (in case you intent to read from HDFS)
   dfs.nameservices
   yarn.resourcemanager.hostname or yarn.resourcemanager.address
   yarn.application.classpath
   core-site.xml
   yarn-site.xml
   hdfs-site.xml
   mapred-site.xml
*/
 System.setProperty("HADOOP_CONF_DIR")=??
 val spark = org.apache.spark.sql.SparkSession.builder.master("yarn").deployMode("client").appName("Spark CSV Reader").getOrCreate;
 //val spark = org.apache.spark.sql.SparkSession.builder.master("spark://sandbox-hdp.hortonworks.com:4040").appName("Spark CSV Reader").getOrCreate;

 // Start operating on the data
 //val df = spark.read.format("csv").option("header", "true").option("mode", "DROPMALFORMED").load("src/main/resources/data-spark/drivers.csv")

}
