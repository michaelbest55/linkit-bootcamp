ThisBuild / scalaVersion :="2.11.8"

lazy val hello = (project in file("."))
  .settings(
    name := "basic_hdfs",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "3.1.1",
    libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.1",
    libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.3.1",
  )
