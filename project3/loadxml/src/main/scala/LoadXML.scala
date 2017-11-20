import org.apache.spark.sql.Dataset
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Encoders
import org.apache.spark.SparkContext._
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types._
import org.apache.spark.sql.types._
import com.databricks.spark.xml._
import spark.implicits._

case class Person (time: Double, id: Long, x: Double, y: Double,
                    angle: Double, pos: Double, speed: Double,
                    slope: Double, edge: String)


case class Vehicle (time: Double, id: Long, x: Double, y: Double, 
                    angle: Double, `type`: String, speed: Double,
                    pos: Double, lane: String, slope: Double)

case class Source (time: Double, person: Array[(String, Double, String,
                    Long, Double, Double, Double, Double, String )],
                  vehicle: Array[(String, Double, Long, String, Double,
                    Double, Double, String, Double, Double)])

object LoadXML {
 
  val spark = SparkSession.builder()
                          .appName("LoadXML")
                          .master("local[9]")
                          .getOrCreate

  val sc = spark.sparkContext
  val sqlContext = new SQLContext(sc)

  // Read XML File
  def loadXML (path: String): Dataset[Source] = {
      spark.read
           .format("com.databricks.spark.xml")
           .option("rowTag", "timestep")
           .load(path)
           .withColumnRenamed("_time", "time")
           .as[Source]
  }

  def personDF(df: Dataset[Source]): Dataset[Person] = {
    df.drop("vehicle")
      .withColumn("person", explode(col("person")))
      .select($"time", $"person._id", $"person._x", $"person._y", $"person._angle",
        $"person._pos", $"person._speed", $"person._slope", $"person._edge")
      .withColumnRenamed("_slope", "slope")
      .withColumnRenamed("_edge", "edge")
      .withColumnRenamed("_speed", "speed")
      .withColumnRenamed("_pos", "pos")
      .withColumnRenamed("_y", "y")
      .withColumnRenamed("_x", "x")
      .withColumnRenamed("_angle", "angle")
      .withColumnRenamed("_id", "id")
      .as[Person]
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
