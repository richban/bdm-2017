import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.sql.Date

import spire.random.mutable.Device

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

case class Readings (did:String, readings:Array[(Array[(String,String,Double,Double,String)],Long)])
case class ExplodedReadings (did: String, readings:(Array[(String,String,Double,Double,String)],Long))
case class FlattenedReadingsInput (did:String, cid:Array[String], clientOS:Array[String], rssi:Array[Double], snRatio:Array[Double], ssid:Array[String], ts:Long)
case class FlattenedReadings (did:String, cid:String, clientOS:String, rssi:Double, snRatio:Double, ssid:String, ts:Long)
case class MetaData (deviceName: String, upTime: String, deviceFunction: String, deviceMode: String, did: String, location: String)

object LoadReadings {

	val spark = SparkSession.builder()
      .master("local[9]")
      .appName("project2-group12")
      .getOrCreate()


	import spark.implicits._

	def loadReadings (path:String): Dataset[Readings] = {
		spark.read
			 .json(path)
			 .as[Readings]
	}

	def loadMetaData (path:String): Dataset[MetaData] = {
		spark.read
			.json(path)
			.as[MetaData]
	}

	def fullFlatten(df:Dataset[FlattenedReadingsInput]) : Dataset[FlattenedReadings] = {
		df.flatMap(row => {
	        val seq = for( i <- 0 until row.cid.size) yield { 
	        	FlattenedReadings(row.did, row.cid(i), row.clientOS(i), row.rssi(i), row.snRatio(i), row.ssid(i), row.ts)
	        }
	        seq.toSeq			
		})
    }

	def flattenDF (df:Dataset[Readings]): Dataset[FlattenedReadingsInput] = {
		val expDF = df.withColumn("readings", explode(col("readings"))).as[ExplodedReadings]
		expDF
			.select($"did",$"readings.clients.cid",$"readings.clients.clientOS",$"readings.clients.rssi",$"readings.clients.snRatio",$"readings.clients.ssid",$"readings.ts")
			.drop("readings")
			.as[FlattenedReadingsInput]
	}

	// Test method, can be called in spark-shell the following way: LoadReadings.test1("8-10-2017.json").show
	def test1 (path:String): Dataset[FlattenedReadings] = {
		val json = loadReadings(path)
		val flat = flattenDF(json)
		fullFlatten(flat).where($"did" === "ed28082926b85c506e3fdb630b6a8bf7")
	}


	def countData(df: Dataset[FlattenedReadings]) = {
		df.withColumn("coulumn", lit(1))
			.as[(String, String, String, Double, Double, String, Long, Integer)]
	  	.groupByKey(_._1)
	  	.reduceGroups((x, y) => (x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8+y._8))
	  	.map(_._2)
	}

	def main(args: Array[String]) = {

    // file paths
    val file1 = "/data/time_series/*.json"
    val file2 = "/data/rooms/*.json"
    val file3 = "/data/meta.json"

//    val hdfs1 = "hdfs:/user/group12/itu_data/wifi_data/*.json"
//    val hdfs2 = "hdfs:/user/group12/itu_data/rooms_data/*.json"
//    val hdfs3 = "hdfs:/user/group12/itu_data/meta.json"

    // Load data
		val wifiData = LoadReadings.loadReadings(file1)
    val roomData = LoadReadings.loadReadings(file2)
		val metaData = LoadReadings.loadMetaData(file3)

    // Flatten wifiData
		val flattenReadings = LoadReadings.fullFlatten(LoadReadings.flattenDF(wifiData))
//
//    // Join wifiData && metaData
//		val mergeData = flattenReadings.join(metaData, "did")
//
//    // Create temporary view 1
//		flattenReadings.createOrReplaceTempView("time_series")
//		val sqlDF = spark.sql("SELECT did, from_unixtime(ts,'dd-MM-YYYY-HH:mm:ss'), " +
//      "count(cid) FROM time_series GROUP BY did,ts ORDER BY 2")
//		sqlDF.show()
//
//    // Create temporary view 2
//		mergeData.createOrReplaceTempView("time_series_location")
//		val sqlDF = spark.sql("SELECT location, from_unixtime(ts,'dd-MM-YYYY') as date, " +
//      "from_unixtime(ts,'HH:mm:ss') as time, count(cid) as num_clients" +
//			" FROM time_series_location GROUP BY location, date, time ORDER BY num_clients DESC")
//		sqlDF.show()
	}

}
