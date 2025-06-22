# Example: Generating a Dataset Partitioned by a Primitive Type Column

## Code:

```scala
val spark = SparkSession.builder().master("local[*]").getOrCreate()
spark.sparkContext.setLogLevel("WARN")

val fields = Map[String, String](
    "title" -> "#{book.title}",
    "author" -> "#{book.author}",
).map { case (field, expr) => s"fields.$field" -> expr }

val schema = StructType(Seq(
    StructField("title", StringType, nullable = false),
    StructField("author",  StringType, nullable = false)
))


val df = spark
  .read
  .format("datafaker")
  .schema(schema)
  .option("numRows", 10)
  .option("partitionsColumn", "author")
  .option("partitionsCount", 3)
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
 |-- author: string (nullable = false)

+---------------------------------------+------------------+
|title                                  |author            |
+---------------------------------------+------------------+
|A Many-Splendoured Thing               |Mrs. Luigi Keebler|
|In Dubious Battle                      |Mrs. Luigi Keebler|
|A Summer Bird-Cage                     |Mrs. Luigi Keebler|
|Stranger in a Strange Land             |Mrs. Luigi Keebler|
|Waiting for the Barbarians             |Mrs. Luigi Keebler|
|Consider the Lilies                    |Mrs. Luigi Keebler|
|Of Human Bondage                       |Mrs. Luigi Keebler|
|I Will Fear No Evil                    |Mrs. Luigi Keebler|
|Endless Night                          |Mrs. Luigi Keebler|
|The Violent Bear It Away               |Mrs. Luigi Keebler|
|All the King's Men                     |Lilian Monahan    |
|When the Green Woods Laugh             |Lilian Monahan    |
|As I Lay Dying                         |Lilian Monahan    |
|The Far-Distant Oxus                   |Lilian Monahan    |
|A Summer Bird-Cage                     |Lilian Monahan    |
|The Line of Beauty                     |Lilian Monahan    |
|Blue Remembered Earth                  |Lilian Monahan    |
|Ring of Bright Water                   |Lilian Monahan    |
|Time To Murder And Create              |Lilian Monahan    |
|Recalled to Life                       |Lilian Monahan    |
|Jacob Have I Loved                     |Cornelius Boyer   |
|In a Dry Season                        |Cornelius Boyer   |
|The Heart Is Deceitful Above All Things|Cornelius Boyer   |
|Postern of Fate                        |Cornelius Boyer   |
|A Passage to India                     |Cornelius Boyer   |
|Consider the Lilies                    |Cornelius Boyer   |
|Of Human Bondage                       |Cornelius Boyer   |
|The Golden Apples of the Sun           |Cornelius Boyer   |
|The House of Mirth                     |Cornelius Boyer   |
|Gone with the Wind                     |Cornelius Boyer   |
+---------------------------------------+------------------+
```

</details>