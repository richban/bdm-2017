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

case class Source (time: Double, person: Array[(String, Double, String,
                    Long, Double, Double, Double, Double, String )],
                  vehicle: Array[(String, Double, Long, String, Double,
                    Double, Double, String, Double, Double)])

object LoadXML {

  val sourceSchema = StructType(StructField("time", DoubleType, false),
    StructField("person", ArrayType(StructType(
      StructField ("_VALUE", StringType, nullable=false),
      StructField ("_angle", DoubleType, nullable=false),
      StructField ("_edge", StringType, nullable=false), 
      StructField ("_id", LongType, nullable=false), 
      StructField ("_pos", DoubleType, nullable=false), 
      StructField ("_slope", DoubleType, nullable=false), 
      StructField ("_speed", DoubleType, nullable=false), 
      StructField ("_x", DoubleType, nullable=false), 
      StructField ("_y", StringType, nullable=false)))),
    StructField("vehicle", ArrayType(StructType(
      StructField ("_VALUE", StringType, nullable=false),
      StructField ("_angle", DoubleType, nullable=false),
      StructField ("_id", LongType, nullable=false), 
      StructField ("_lane", StringType, nullable=false), 
      StructField ("_pos", DoubleType, nullable=false), 
      StructField ("_slope", DoubleType, nullable=false), 
      StructField ("_speed", DoubleType, nullable=false), 
      StructField ("_type", StringType, nullable=false), 
      StructField ("_x", DoubleType, nullable=false), 
      StructField ("_y", DoubleType, nullable=false)))))

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
  def loadXML (path: String): Dataset[Source] = {
      spark.read
           .format("com.databricks.spark.xml")
           .option("rowTag", "timestep")
           .load(path)
           .as[Source]
  }

  // Flatten Vehicle
}

object Main extends App {

}
