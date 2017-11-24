import org.apache.spark.sql.Dataset
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Encoders
import org.apache.spark.SparkContext._
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types._
import org.apache.spark.sql.types._
import com.databricks.spark.xml._
import org.apache.spark.sql._
import spark.implicits._

case class Person (time: Double, id: Long, x: Double, y: Double,
                    angle: Double, position: Double, speed: Double,
                    slope: Double, movement: String, edge: String,
                    edge_type: String, edge_lane: String)


case class Vehicle (time: Double, id: Long, x: Double, y: Double, 
                    angle: Double, `type`: String, speed: Double,
                    pos: Double, lane: String, slope: Double)

case class Source (time: Double, person: Array[(String, Double, String,
                    Long, Double, Double, Double, Double, String )],
                  vehicle: Array[(String, Double, Long, String, Double,
                    Double, Double, String, Double, Double)])

type ParsedEdge = (String, String, String, String)

type PersonCol = (Double, Long, Double, Double,
                  Double, Double, Double, Double,
                  String, String, String, String)
object LoadXML extends java.io.Serializable {
 
  val spark = SparkSession.builder()
                          .appName("LoadXML")
                          .master("local[9]")
                          .getOrCreate

  val sc = spark.sparkContext
  val sqlContext = new SQLContext(sc)

  def getEdgeData(t: String, m: String, e: String, l: String): ParsedEdge = {
    val edge_type =Option(t).map(_ => "intersection").getOrElse("road")
    val movement = Option(m).map(_ => "backward").getOrElse("forward")
    (edge_type, movement, e, l)
  }

  def parseEdge(edge: String): ParsedEdge = {
    val Pattern = raw"^(:)?(-)?(\d+)_(\d).*$$".r
    edge match {
      case Pattern(t, m, e, l) => getEdgeData(t, m, e, l)
      case _ => null
    }
  }

   // Read XML File
  def loadXML (path: String): Dataset[Source] = {
      spark.read
           .format("com.databricks.spark.xml")
           .option("rowTag", "timestep")
           .load(path)
           .withColumnRenamed("_time", "time")
           .as[Source]
  }

  def personDF(df: Dataset[Source]): Dataset[Person] = df.drop("vehicle")
    .withColumn("person", explode(col("person")))
    .select($"time", $"person._id", $"person._x", $"person._y", $"person._angle",
      $"person._pos", $"person._speed", $"person._slope", $"person._edge")
    .map(r => {
      val (edge_type, movement, edge, edge_lane) = parseEdge(r getString 8)
      (r getDouble 0, r getLong 1, r getDouble 2, r getDouble 3,
        r getDouble 4, r getDouble 5, r getDouble 6, r getDouble 7,
        movement, edge, edge_type, edge_lane)})
    .withColumnRenamed("_1", "time")
    .withColumnRenamed("_2", "id")
    .withColumnRenamed("_3", "x")
    .withColumnRenamed("_4", "y")
    .withColumnRenamed("_5", "angle")
    .withColumnRenamed("_6", "position")
    .withColumnRenamed("_7", "speed")
    .withColumnRenamed("_8", "slope")
    .withColumnRenamed("_9", "movement")
    .withColumnRenamed("_10", "edge")
    .withColumnRenamed("_11", "edge_type")
    .withColumnRenamed("_12", "edge_lane")
    .as[Person]

  def personCol(df: Dataset[Source]) =
    df.drop("vehicle")
    .withColumn("person", explode(col("person")))
    .select($"time", $"person._id", $"person._x", $"person._y", $"person._angle",
      $"person._pos", $"person._speed", $"person._slope", $"person._edge")
    .as[(Double, Long, Double, Double, Double, Double, Double, Double, String)]
    .flatMap { case (time, _id, _x, _y, _angle, _pos, _speed, _slope, _edge) =>
      val (edge_type, movement, edge, edge_lane) = parseEdge(_edge)
      val seq = for(i <- 0 until 10) yield {
        Person(time, _id, _x, _y, _angle, _pos, _speed, _slope, edge_type, movement, edge, edge_lane)
      }
      seq.toSeq
    }


  def vehicleDF(df: Dataset[Source]): Dataset[Vehicle] = {
      df.drop("person")
        .withColumn("vehicle", explode(col("vehicle")))
        .select($"time", $"vehicle._id", $"vehicle._x", $"vehicle._y",
          $"vehicle._angle", $"vehicle._pos", $"vehicle._speed", 
          $"vehicle._slope", $"vehicle._lane", $"vehicle._type")
        .withColumnRenamed("_slope", "slope")
        .withColumnRenamed("_lane", "lane")
        .withColumnRenamed("_speed", "speed")
        .withColumnRenamed("_pos", "pos")
        .withColumnRenamed("_y", "y")
        .withColumnRenamed("_x", "x")
        .withColumnRenamed("_angle", "angle")
        .withColumnRenamed("_id", "id")
        .withColumnRenamed("_type", "type")
        .as[Vehicle]
    }

}

object Main extends App {

}
