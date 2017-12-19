import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.types._
import org.apache.spark.sql.types._
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.sql.Date

import com.sun.tools.internal.ws.processor.model.java.JavaStructureType
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

case class Readings (did:String, readings:Array[(Array[(String,String,Double,Double,String)],Long)])

case class ExplodedReadings (did: String, readings:(Array[(String, String,Double,Double,String)],Long))

case class FlattenedReadingsInput (did:String, cid:Array[String], clientOS:Array[String], rssi:Array[Double],
																	 snRatio:Array[Double], ssid:Array[String], ts:Long)

case class FlattenedReadings (did:String, cid:String, clientOS:String, rssi:Double,
															snRatio:Double, ssid:String, ts:Long)

case class MetaData (deviceName: String, upTime: String, deviceFunction: String, deviceMode: String,
										 did: String, location: String)

case class Rooms (name: String, startDate: String, endDate: String, startTime: String,
                  endTime: String, room: String, `type`: String,
                  lecturers: String, programme: String)


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

  def loadRooms (path:String): Dataset[Rooms] = {
    spark.read
      .json(path)
      .as[Rooms]
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

    val hdfs1 = "itu_data/wifi_data/*.json"
    val hdfs2 = "itu_data/rooms_data/*.json"
    val hdfs3 = "itu_data/meta.json"

    // Load data
		val wifiData = LoadReadings.loadReadings(hdfs1)
    val roomData = LoadReadings.loadRooms(hdfs2)
		val metaData = LoadReadings.loadMetaData(hdfs3)

//    // Flatten wifiData
		val flattenReadings = LoadReadings.fullFlatten(LoadReadings.flattenDF(wifiData))
//
//    // Join wifiData && metaData
		val mergeData = flattenReadings.join(metaData, "did")

    // Join Wifi Data && Rooms
    val wifiRoom = mergeData.join(roomData, $"location" === $"room")

    // Create temporary views
		flattenReadings.createOrReplaceTempView("time_series")
    mergeData.createOrReplaceTempView("time_series_location")
    wifiRoom.createOrReplaceTempView("wifi_room")


    // View1 Number of connections to an access point at a certain time
    val view1 = spark.sql("SELECT did, location, from_unixtime(ts, 'dd-MM-YYYY') as date," +
      "from_unixtime(ts, 'HH:mm:ss') as time, count(cid) num_clients_connected" +
      " FROM time_series_location GROUP BY did, location, date, time")

    // View2 the average signal strength of users to an access point
    val view2 = spark.sql("SELECT did, cid, avg(rssi) as avg_signal_strength FROM time_series_location GROUP BY 1, 2")

  }
}
