# Example: Generating a Dataset Partitioned by a MapType Column

## Code:

```scala
val spark = SparkSession.builder().master("local[*]").getOrCreate()
spark.sparkContext.setLogLevel("WARN")

import FieldConfig.implicits._

val fields = Map[String, String](
    "title" -> "#{book.title}",
    "author" -> FieldConfig(key = "#{book.author}", value = "#{number.numberBetween '1900', '2000'}", count = 2),
).map { case (field, expr) => s"fields.$field" -> expr }

val schema = StructType(Seq(
    StructField("title", StringType, nullable = false),
    StructField(
        "author",
        MapType(StringType, IntegerType),
        nullable = false
    )
))


val df = spark
  .read
  .format("datafaker")
  .schema(schema)
  .option("numRows", 10)
  .options(fields)
  .option("partitionsColumn", "author")
  .option("partitionsCount", 3)
  .load()

df.printSchema()
df.show(30, false)
```

## Output:
<details>
<summary>See</summary>

```
root
 |-- title: string (nullable = false)
 |-- author: map (nullable = false)
 |    |-- key: string
 |    |-- value: integer (valueContainsNull = true)

+---------------------------------------+----------------------------------------------------+
|title                                  |author                                              |
+---------------------------------------+----------------------------------------------------+
|The Golden Bowl                        |{Ms. Kiana Monahan -> 1970, Sina Stiedemann -> 1935}|
|Quo Vadis                              |{Ms. Kiana Monahan -> 1970, Sina Stiedemann -> 1935}|
|Consider Phlebas                       |{Ms. Kiana Monahan -> 1970, Sina Stiedemann -> 1935}|
|I Sing the Body Electric               |{Ms. Kiana Monahan -> 1970, Sina Stiedemann -> 1935}|
|His Dark Materials                     |{Ms. Kiana Monahan -> 1970, Sina Stiedemann -> 1935}|
|Antic Hay                              |{Ms. Kiana Monahan -> 1970, Sina Stiedemann -> 1935}|
|If Not Now, When?                      |{Ms. Kiana Monahan -> 1970, Sina Stiedemann -> 1935}|
|From Here to Eternity                  |{Ms. Kiana Monahan -> 1970, Sina Stiedemann -> 1935}|
|The World, the Flesh and the Devil     |{Ms. Kiana Monahan -> 1970, Sina Stiedemann -> 1935}|
|The Cricket on the Hearth              |{Ms. Kiana Monahan -> 1970, Sina Stiedemann -> 1935}|
|The Doors of Perception                |{Ezekiel Kulas -> 1972, Linn Collier IV -> 1912}    |
|A Time to Kill                         |{Ezekiel Kulas -> 1972, Linn Collier IV -> 1912}    |
|The Doors of Perception                |{Ezekiel Kulas -> 1972, Linn Collier IV -> 1912}    |
|The Man Within                         |{Ezekiel Kulas -> 1972, Linn Collier IV -> 1912}    |
|The Moving Finger                      |{Ezekiel Kulas -> 1972, Linn Collier IV -> 1912}    |
|The Heart Is Deceitful Above All Things|{Ezekiel Kulas -> 1972, Linn Collier IV -> 1912}    |
|The Last Enemy                         |{Ezekiel Kulas -> 1972, Linn Collier IV -> 1912}    |
|Edna O'Brien                           |{Ezekiel Kulas -> 1972, Linn Collier IV -> 1912}    |
|The Heart Is a Lonely Hunter           |{Ezekiel Kulas -> 1972, Linn Collier IV -> 1912}    |
|Eyeless in Gaza                        |{Ezekiel Kulas -> 1972, Linn Collier IV -> 1912}    |
|The Golden Bowl                        |{Dr. Carlotta Dare -> 1908, Chan Stroman -> 1982}   |
|To a God Unknown                       |{Dr. Carlotta Dare -> 1908, Chan Stroman -> 1982}   |
|Mother Night                           |{Dr. Carlotta Dare -> 1908, Chan Stroman -> 1982}   |
|Frequent Hearses                       |{Dr. Carlotta Dare -> 1908, Chan Stroman -> 1982}   |
|From Here to Eternity                  |{Dr. Carlotta Dare -> 1908, Chan Stroman -> 1982}   |
|Fame Is the Spur                       |{Dr. Carlotta Dare -> 1908, Chan Stroman -> 1982}   |
|Now Sleeps the Crimson Petal           |{Dr. Carlotta Dare -> 1908, Chan Stroman -> 1982}   |
|Françoise Sagan                        |{Dr. Carlotta Dare -> 1908, Chan Stroman -> 1982}   |
|O Pioneers!                            |{Dr. Carlotta Dare -> 1908, Chan Stroman -> 1982}   |
|The Proper Study                       |{Dr. Carlotta Dare -> 1908, Chan Stroman -> 1982}   |
+---------------------------------------+----------------------------------------------------+
```

</details>