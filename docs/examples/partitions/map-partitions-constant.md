# Example: Generating a Dataset Partitioned by a Constant MapType Column

## Code:

```scala
val spark = SparkSession.builder().master("local[*]").getOrCreate()
spark.sparkContext.setLogLevel("WARN")

val fields = Map[String, String](
    "title" -> "#{book.title}"
).map { case (field, expr) => s"fields.$field" -> expr }

val schema = StructType(Seq(
    StructField("title", StringType, nullable = false),
    StructField(
        "author",
        MapType(StringType, IntegerType),
        nullable = false
    )
))

val partitionsValues = Seq(
    Map("Author I" -> "1991", "Another Author 1" -> "1981").asJson.noSpaces,
    Map("Author II" -> "1992", "Another Author 2" -> "1982").asJson.noSpaces,
    Map("Author III" -> "1993", "Another Author 3" -> "1983").asJson.noSpaces
).asJson.noSpaces

val df = spark
  .read
  .format("datafaker")
  .schema(schema)
  .option("numRows", 10)
  .options(fields)
  .option("partitionsColumn", "author")
  .option("partitionsValues", partitionsValues)
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

+---------------------------------------+----------------------------------------------+
|title                                  |author                                        |
+---------------------------------------+----------------------------------------------+
|An Acceptable Time                     |{Author I -> 1991, Another Author 1 -> 1981}  |
|Stranger in a Strange Land             |{Author I -> 1991, Another Author 1 -> 1981}  |
|The Wives of Bath                      |{Author I -> 1991, Another Author 1 -> 1981}  |
|The Other Side of Silence              |{Author I -> 1991, Another Author 1 -> 1981}  |
|Everything is Illuminated              |{Author I -> 1991, Another Author 1 -> 1981}  |
|The Wives of Bath                      |{Author I -> 1991, Another Author 1 -> 1981}  |
|All the King's Men                     |{Author I -> 1991, Another Author 1 -> 1981}  |
|Tirra Lirra by the River               |{Author I -> 1991, Another Author 1 -> 1981}  |
|The Heart Is Deceitful Above All Things|{Author I -> 1991, Another Author 1 -> 1981}  |
|Everything is Illuminated              |{Author I -> 1991, Another Author 1 -> 1981}  |
|The Grapes of Wrath                    |{Author II -> 1992, Another Author 2 -> 1982} |
|After Many a Summer Dies the Swan      |{Author II -> 1992, Another Author 2 -> 1982} |
|Down to a Sunless Sea                  |{Author II -> 1992, Another Author 2 -> 1982} |
|Down to a Sunless Sea                  |{Author II -> 1992, Another Author 2 -> 1982} |
|Recalled to Life                       |{Author II -> 1992, Another Author 2 -> 1982} |
|In a Glass Darkly                      |{Author II -> 1992, Another Author 2 -> 1982} |
|Nine Coaches Waiting                   |{Author II -> 1992, Another Author 2 -> 1982} |
|The Wings of the Dove                  |{Author II -> 1992, Another Author 2 -> 1982} |
|Frequent Hearses                       |{Author II -> 1992, Another Author 2 -> 1982} |
|The Golden Bowl                        |{Author II -> 1992, Another Author 2 -> 1982} |
|When the Green Woods Laugh             |{Author III -> 1993, Another Author 3 -> 1983}|
|Butter In a Lordly Dish                |{Author III -> 1993, Another Author 3 -> 1983}|
|Unweaving the Rainbow                  |{Author III -> 1993, Another Author 3 -> 1983}|
|The Road Less Traveled                 |{Author III -> 1993, Another Author 3 -> 1983}|
|Cabbages and Kings                     |{Author III -> 1993, Another Author 3 -> 1983}|
|To Your Scattered Bodies Go            |{Author III -> 1993, Another Author 3 -> 1983}|
|Look Homeward, Angel                   |{Author III -> 1993, Another Author 3 -> 1983}|
|All the King's Men                     |{Author III -> 1993, Another Author 3 -> 1983}|
|Recalled to Life                       |{Author III -> 1993, Another Author 3 -> 1983}|
|Arms and the Man                       |{Author III -> 1993, Another Author 3 -> 1983}|
+---------------------------------------+----------------------------------------------+
```

</details>