# Example: Generating a Dataset Partitioned by a Constant ArrayType Column

## Code:

```scala
val spark = SparkSession.builder().master("local[*]").getOrCreate()
spark.sparkContext.setLogLevel("WARN")

val fields = Map[String, String](
    "title" -> "#{book.title}"
).map { case (field, expr) => s"fields.$field" -> expr }

val schema = StructType(Seq(
    StructField("title", StringType, nullable = false),
    StructField("translations", ArrayType(StringType), nullable = false),
))

val partitionsValues = Seq(
  Seq("Part1_1", "Part1_2").asJson.noSpaces,
  Seq("Part2_1", "Part2_2").asJson.noSpaces,
  Seq("Part3_1", "Part3_2").asJson.noSpaces
).asJson.noSpaces

val df = spark
  .read
  .format("datafaker")
  .schema(schema)
  .option("numRows", 10)
  .option("partitionsColumn", "translations")
  .option("partitionsValues", partitionsValues)
  .options(fields)
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
 |-- translations: array (nullable = false)
 |    |-- element: string (containsNull = true)

+--------------------------+------------------+
|title                     |translations      |
+--------------------------+------------------+
|The Man Within            |[Part1_1, Part1_2]|
|No Country for Old Men    |[Part1_1, Part1_2]|
|Clouds of Witness         |[Part1_1, Part1_2]|
|Little Hands Clapping     |[Part1_1, Part1_2]|
|Time To Murder And Create |[Part1_1, Part1_2]|
|The Way Through the Woods |[Part1_1, Part1_2]|
|The Lathe of Heaven       |[Part1_1, Part1_2]|
|Consider Phlebas          |[Part1_1, Part1_2]|
|The Green Bay Tree        |[Part1_1, Part1_2]|
|Shall not Perish          |[Part1_1, Part1_2]|
|Behold the Man            |[Part2_1, Part2_2]|
|Consider the Lilies       |[Part2_1, Part2_2]|
|Behold the Man            |[Part2_1, Part2_2]|
|A Darkling Plain          |[Part2_1, Part2_2]|
|Françoise Sagan           |[Part2_1, Part2_2]|
|Where Angels Fear to Tread|[Part2_1, Part2_2]|
|The Proper Study          |[Part2_1, Part2_2]|
|Far From the Madding Crowd|[Part2_1, Part2_2]|
|The Far-Distant Oxus      |[Part2_1, Part2_2]|
|An Evil Cradling          |[Part2_1, Part2_2]|
|Dance Dance Dance         |[Part3_1, Part3_2]|
|Mr Standfast              |[Part3_1, Part3_2]|
|Down to a Sunless Sea     |[Part3_1, Part3_2]|
|That Hideous Strength     |[Part3_1, Part3_2]|
|Death Be Not Proud        |[Part3_1, Part3_2]|
|This Side of Paradise     |[Part3_1, Part3_2]|
|For Whom the Bell Tolls   |[Part3_1, Part3_2]|
|A Darkling Plain          |[Part3_1, Part3_2]|
|The Wings of the Dove     |[Part3_1, Part3_2]|
|Blithe Spirit             |[Part3_1, Part3_2]|
+--------------------------+------------------+
```

</details>