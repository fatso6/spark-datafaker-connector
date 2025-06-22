# Example: Generating a Dataset Partitioned by a Constant Primitive Type Column

## Code:

```scala
val spark = SparkSession.builder().master("local[*]").getOrCreate()
spark.sparkContext.setLogLevel("WARN")

val fields = Map[String, String](
    "title" -> "#{book.title}"
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
  .option("partitionsValues", """["Author I", "Author II", "Author III"]""")
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

+------------------------------------+----------+
|title                               |author    |
+------------------------------------+----------+
|East of Eden                        |Author I  |
|Dying of the Light                  |Author I  |
|If Not Now, When?                   |Author I  |
|The Moving Finger                   |Author I  |
|The Cricket on the Hearth           |Author I  |
|Fear and Trembling                  |Author I  |
|Of Human Bondage                    |Author I  |
|The Wives of Bath                   |Author I  |
|The Green Bay Tree                  |Author I  |
|Fame Is the Spur                    |Author I  |
|Everything is Illuminated           |Author II |
|A Summer Bird-Cage                  |Author II |
|Taming a Sea Horse                  |Author II |
|All Passion Spent                   |Author II |
|Carrion Comfort                     |Author II |
|Stranger in a Strange Land          |Author II |
|Have His Carcase                    |Author II |
|A Time of Gifts                     |Author II |
|The Man Within                      |Author II |
|The Mirror Crack'd from Side to Side|Author II |
|Ego Dominus Tuus                    |Author III|
|Everything is Illuminated           |Author III|
|A Monstrous Regiment of Women       |Author III|
|Quo Vadis                           |Author III|
|To a God Unknown                    |Author III|
|Tiger! Tiger!                       |Author III|
|In a Dry Season                     |Author III|
|Sleep the Brave                     |Author III|
|If I Forget Thee Jerusalem          |Author III|
|The Little Foxes                    |Author III|
+------------------------------------+----------+
```

</details>