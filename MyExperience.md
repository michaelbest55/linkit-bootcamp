## My experience and further notes

Hello! My name is Michael Best. Thanks for taking the time to read through my thoughts on the exercise! I have to admit that I ran into a lot of small prblems that ended up taking a lot longer time to fix than I would've liked, so sorry if it is quite incomplete.

Before I took any steps towards working in the sandbox, I took the opportunity to set up a cloud environment. I had never done so, so I figured this was a good opportunity, not to mention that I have a small old Lenovo laptop which desperately needs some more RAM memory :)

## HDP Sandbox

Ok, so first I had to get a AWS instance going. After connecting to an Amazon Linux EC2 instance, I tried running the deployment script for the sandbox environment, which basically sets up some docker images on which then we work in. I ran into the error: "cgroups: cannot found cgroup mount destination: unknown". Cgroups are a kernel feature that isolate resources for a hierearchy of processes, and are supposed to be set up by cgroups. As far as I could tell, this is used in containers a lot, with several people having documented the error in docker 18. 
To fix this I tried the following commands, suggested in https://github.com/docker/for-linux/issues/219

```
sudo mkdir /sys/fs/cgroup/systemd
sudo mount -t cgroup -o none,name=systemd cgroup /sys/fs/cgroup/systemd
```

But got the following error: start returned error: unable to find \"systemd\" in controller set: unknown
I could not understand how to fix this after doing some googling.

In https://github.com/docker/for-linux/issues/469#issuecomment-437361309, they suggested 
```
rm -fr /sys/fs/cgroup
mount -t cgroup -o none,name=systemd cgroup /sys/fs/cgroup/systemd
```
This did not work either. 

In all the pages I opened, people were saying they could fix this problem by using docker version 17.09.1, which is not available in the amazon image, so i ended up making a new instance with ubuntu 18.04 on it. The next step was to connect to the Sandbox. In order to get a GUI I connected to the server with ssh -XC. After finally getting a working version of the sandbox working I could get to the dashboard. 

## HDFS & Hive on Spark

I spent a lot of time trying to understand how to connect to Spark via the scala API. I tried first to connect to SPARK as if it were a standalone cluster on the Sandbox. It took me a long time to realize that this doesn't seem to be the default setup. Even with some googling and looking at the Spark default configs in the Ambari viewer I could not really understand in what way Spark is configured. I figured it was worth a shot trying to solve the problem in the sparl-shell, which I managed to finish successfully. The commands used can be found in one of the sections below.

Eventually I reached out to Thiago who suggested to submit a jar file to spark-submit, which I built using sbt. This definitely seemed to work more successfully than when I tried to connect directly to the Sandbox in the scala app.

I could not import all the csv files without any issues. For some reason Spark could not correctly parse the truck_event_text_partition.csv. I tried checking how it was delimiting the columns, and tried feeding it with a Schema where it interpreted everything as string in the hope that it would ignore potential hidden characters. This didn't work, it kept complaining about going over the maximum number of columns.

When I tried to create Hive Tables, I got a variety of messages with Hive: Cannot get a table snapshot for timesheettable
After googling for a while I could not figure out what the olution to this was.


## HBase
To be honest I've run out of time in order to try and solve this using a scala app. The best I can offer is a poor attempt to make things work using the HBase shell line, where the commands used can be found in the last section.
From the bit of digging around I managed to do, I saw that HortonWorks has an API to go from spark to HBase, https://github.com/hortonworks-spark/shc. 



## Spark commands in shell

```
# Upload the files as Spark DataFrames
val df = spark.read.format("csv").option("header","true").load("my_files/data-spark/drivers.csv")
val df2 = spark.read.format("csv").option("header","true").load("my_files/data-spark/timesheet.csv")
val df3 = spark.read.format("csv").option("header","true").load("my_files/data-spark/truck_event_text_partition.csv")
# Create Hive Tables for each DataFrame
df.createOrReplaceTempView("driversTempTable")
spark.sqlContext.sql("create table driversTable as select * from driversTempTable")
df2.createOrReplaceTempView("timesheetTempTable")
spark.sqlContext.sql("create table timesheetTable as select * from timesheetTempTable")
df3.createOrReplaceTempView("truckeventTempTable")
spark.sqlContext.sql("create table truckEventTable as select * from truckEventTempTable")
# Join hive tables
spark.sql("SELECT timeSheetTable.driverId, name, `hours-logged`, `miles-logged`  FROM timeSheetTable JOIN driversTable WHERE driversTable.driverId=timeSheetTable.driverId").show()
```
# Resulting top 20 rows in table
                    
|driverId|name|hours-logged|miles-logged|
| --- | --- | --- | --- |
|      10|George Vetticaden|          70|        3300|
|      10|George Vetticaden|          70|        3300|
|      10|George Vetticaden|          60|        2800|
|      10|George Vetticaden|          70|        3100|
|      10|George Vetticaden|          70|        3200|
|      10|George Vetticaden|          70|        3300|
|      10|George Vetticaden|          70|        3000|
|      10|George Vetticaden|          70|        3300|
|      10|George Vetticaden|          70|        3200|
|      10|George Vetticaden|          50|        2500|
|      10|George Vetticaden|          70|        2900|
|      10|George Vetticaden|          70|        3100|
|      10|George Vetticaden|          70|        3300|
|      10|George Vetticaden|          70|        3300|
|      10|George Vetticaden|          70|        3300|
|      10|George Vetticaden|          70|        3400|
|      10|George Vetticaden|          70|        3300|
|      10|George Vetticaden|          70|        3300|
|      10|George Vetticaden|          70|        3300|
|      10|George Vetticaden|          30|        1200|



## HBase shell commands
The following commands were run logged into the Sandbox as maria_dev
```
#This creates the hbase tables
echo "create 'dangerous_driving', 'employee'" | hbase shell; echo "create 'extra_driver', 'employee'" | hbase shell; echo "create 'mergedTable', 'employee'" | hbase shell;

# Load the csv files as HBase tables
hbase org.apache.hadoop.hbase.mapreduce.ImportTsv -Dimporttsv.separator=,  -Dimporttsv.columns="HBASE_ROW_KEY,eventId,driverId,driverName,eventTime,eventType,latitudeColumn,longitudeColumn,routeId,routeName,truckId" dangerous_driving hdfs://sandbox.hortonworks.com:/my_files/data-hbase/dangerous-driver.csv
hbase org.apache.hadoop.hbase.mapreduce.ImportTsv -Dimporttsv.separator=,  -Dimporttsv.columns="HBASE_ROW_KEY,eventId,driverId,driverName,eventTime,eventType,latitudeColumn,longitudeColumn,routeId,routeName,truckId" extra_driver hdfs://sandbox.hortonworks.com:/my_files/data-hbase/extra-driver.csv

hbase(main):001:0> put 'extra_driver','row1','eventId','4'
```
```
#Merge the tables, this still isn't working, for some reason it adds shifts all the data one column over 
hbase org.apache.hadoop.hbase.mapreduce.CopyTable --new.name=mergedTable dangerous_driving
hbase org.apache.hadoop.hbase.mapreduce.CopyTable --new.name=mergedTable extra_driver

# Had the commands above worked, I would guess this works
hbase(main):002:0>put 'extra_driver','row4','routeName','Los Angeles to Santa Clara'
```
