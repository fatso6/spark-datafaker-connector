# Example: Generating a Dataset Partitioned by a StructType Column

## Code:

```scala
val spark = SparkSession.builder().master("local[*]").getOrCreate()
spark.sparkContext.setLogLevel("WARN")

import FieldConfig.implicits._

val fields = Map[String, String](
    "title" -> "#{book.title}",
    "author.name" -> "#{book.author}",
    "author.birth_year" -> "#{number.numberBetween '1900', '2000'}",
    "author.nationalities" -> FieldConfig(expression = "#{address.countryCode}", count = 2),
).map { case (field, expr) => s"fields.$field" -> expr }

val schema = StructType(Seq(
    StructField("title", StringType, nullable = false),

    StructField("author", StructType(Seq(
        StructField("name", StringType),
        StructField("birth_year", IntegerType),
        StructField("nationalities", ArrayType(StringType))
    )), nullable = false)
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
 |-- author: struct (nullable = false)
 |    |-- name: string (nullable = true)
 |    |-- birth_year: integer (nullable = true)
 |    |-- nationalities: array (nullable = true)
 |    |    |-- element: string (containsNull = true)

+--------------------------------------------+--------------------------------------+
|title                                       |author                                |
+--------------------------------------------+--------------------------------------+
|Gone with the Wind                          |{Mr. Annamae Smitham, 1949, [KN, KI]} |
|In Death Ground                             |{Mr. Annamae Smitham, 1949, [KN, KI]} |
|Jesting Pilate                              |{Mr. Annamae Smitham, 1949, [KN, KI]} |
|The Daffodil Sky                            |{Mr. Annamae Smitham, 1949, [KN, KI]} |
|The Mirror Crack'd from Side to Side        |{Mr. Annamae Smitham, 1949, [KN, KI]} |
|Let Us Now Praise Famous Men                |{Mr. Annamae Smitham, 1949, [KN, KI]} |
|The Way of All Flesh                        |{Mr. Annamae Smitham, 1949, [KN, KI]} |
|Bury My Heart at Wounded Knee               |{Mr. Annamae Smitham, 1949, [KN, KI]} |
|Françoise Sagan                             |{Mr. Annamae Smitham, 1949, [KN, KI]} |
|To a God Unknown                            |{Mr. Annamae Smitham, 1949, [KN, KI]} |
|The Wealth of Nations                       |{Yee Champlin, 1949, [SK, SG]}        |
|Those Barren Leaves, Thrones, Dominations   |{Yee Champlin, 1949, [SK, SG]}        |
|By Grand Central Station I Sat Down and Wept|{Yee Champlin, 1949, [SK, SG]}        |
|The World, the Flesh and the Devil          |{Yee Champlin, 1949, [SK, SG]}        |
|A Time to Kill                              |{Yee Champlin, 1949, [SK, SG]}        |
|Unweaving the Rainbow                       |{Yee Champlin, 1949, [SK, SG]}        |
|O Jerusalem!                                |{Yee Champlin, 1949, [SK, SG]}        |
|A Time of Gifts                             |{Yee Champlin, 1949, [SK, SG]}        |
|A Handful of Dust                           |{Yee Champlin, 1949, [SK, SG]}        |
|From Here to Eternity                       |{Yee Champlin, 1949, [SK, SG]}        |
|Little Hands Clapping                       |{Patrick Thompson Sr., 1958, [PH, HR]}|
|From Here to Eternity                       |{Patrick Thompson Sr., 1958, [PH, HR]}|
|Arms and the Man                            |{Patrick Thompson Sr., 1958, [PH, HR]}|
|Far From the Madding Crowd                  |{Patrick Thompson Sr., 1958, [PH, HR]}|
|Blue Remembered Earth                       |{Patrick Thompson Sr., 1958, [PH, HR]}|
|A Summer Bird-Cage                          |{Patrick Thompson Sr., 1958, [PH, HR]}|
|Time of our Darkness                        |{Patrick Thompson Sr., 1958, [PH, HR]}|
|Number the Stars                            |{Patrick Thompson Sr., 1958, [PH, HR]}|
|Tirra Lirra by the River                    |{Patrick Thompson Sr., 1958, [PH, HR]}|
|Carrion Comfort                             |{Patrick Thompson Sr., 1958, [PH, HR]}|
+--------------------------------------------+--------------------------------------+
```

</details>