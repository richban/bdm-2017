import org.apache.spark.sql.Dataset
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Encoders
import org.apache.spark.SparkContext._
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types._
import org.apache.spark.sql.types._
import com.databricks.spark.xml._
import com.databricks.spark.csv._
import spark.implicits._

val personShema = StructType(Array(
  StructField ("id", LongType, nullable=false),
  StructField ("x", DoubleType, nullable=false), 
  StructField ("y", DoubleType, nullable=false), 
  StructField ("angle", DoubleType, nullable=false), 
  StructField ("pos", DoubleType, nullable=false), 
  StructField ("speed", DoubleType, nullable=false), 
  StructField ("slope", DoubleType, nullable=false), 
  StructField ("edge", StringType, nullable=false)))

case class Person (id: Long, x: Double, y: Double,
                    angle: Double, pos: Double, speed: Double,
                    slope: Double, edge: String)

case class Vehicle (time: Double, id: BigInt, x: Double, y: Double, 
                    z: Double, angle: Double, `type`: String, speed: Double,
                    pos: Double, lane: String, slope: Double)

object LoadXML {

  val vehicleSchema = StructType(Array(
    StructField ("time", DoubleType, nullable=false),
    StructField ("id", LongType, nullable=false),
    StructField ("x", DoubleType, nullable=false), 
    StructField ("y", DoubleType, nullable=false), 
    StructField ("angle", DoubleType, nullable=false), 
    StructField ("pos", DoubleType, nullable=false), 
    StructField ("speed", DoubleType, nullable=false), 
    StructField ("slope", DoubleType, nullable=false), 
    StructField ("edge", StringType, nullable=false), 
    StructField ("lane", StringType, nullable=false), 
    StructField ("`type`", StringType, nullable=false)))


  val spark = SparkSession.builder()
                          .appName("LoadXML")
                          .master("local[9]")
                          .getOrCreate

  val sc = spark.sparkContext
  val sqlContext = new SQLContext(sc)

  // Read XML File
  def loadXML (path: String): Dataset[Vehicle] = {
      spark.read
                .format("com.databricks.spark.csv")
                .option("header", "true")
                // .option("inferSchema", "true")
                .option("delimiter", ";")
                .load(path)
                .rdd
                .map { r => (r.timestep_time, r.vehicle_x, r.vehicle_y,
                  r.vehicle_z, r.vehicle_angle, r.vehicle_type, r.vehicle_speed,
                  r.vehicle_pos, r.vehicle_sloper) }
                .toDS
                .withColumnRenamed("_1", "time")
                .withColumnRenamed("_2", "id")
                .withColumnRenamed("_3", "x")
                .withColumnRenamed("_4", "y")
                .withColumnRenamed("_5", "z")
                .withColumnRenamed("_6", "angle")
                .withColumnRenamed("_7", "type")
                .withColumnRenamed("_8", "speed")
                .withColumnRenamed("_9", "pos")
                .withColumnRenamed("_10", "lane")
                .withColumnRenamed("_11", "slope")
                .as[Vehicle]
  }

  // Flatten Vehicle
}

object Main extends App {

}
