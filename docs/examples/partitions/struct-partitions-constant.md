# Example: Generating a Dataset Partitioned by a Constant StructType Column

## Code:

```scala
val spark = SparkSession.builder().master("local[*]").getOrCreate()
spark.sparkContext.setLogLevel("WARN")

val fields = Map[String, String](
  "title" -> "#{book.title}"
).map { case (field, expr) => s"fields.$field" -> expr }

val schema = StructType(Seq(
  StructField("title", StringType, nullable = false),
  StructField("author", StructType(Seq(
    StructField("name", StringType),
    StructField("birth_year", IntegerType)
  )), nullable = false)
))

val partitionsValues = Seq(
  Map("name" -> "Author I", "birth_year" -> "1991").asJson.noSpaces,
  Map("name" -> "Author II", "birth_year" -> "1992").asJson.noSpaces,
  Map("name" -> "Author III", "birth_year" -> "1993").asJson.noSpaces
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
 |-- author: struct (nullable = false)
 |    |-- name: string (nullable = true)
 |    |-- birth_year: integer (nullable = true)

+-------------------------------+------------------+
|title                          |author            |
+-------------------------------+------------------+
|A Many-Splendoured Thing       |{Author I, 1991}  |
|Gone with the Wind             |{Author I, 1991}  |
|Behold the Man                 |{Author I, 1991}  |
|The Millstone                  |{Author I, 1991}  |
|Rosemary Sutcliff              |{Author I, 1991}  |
|Precious Bane                  |{Author I, 1991}  |
|Shall not Perish               |{Author I, 1991}  |
|The Heart Is a Lonely Hunter   |{Author I, 1991}  |
|A Many-Splendoured Thing       |{Author I, 1991}  |
|Things Fall Apart              |{Author I, 1991}  |
|A Monstrous Regiment of Women  |{Author II, 1992} |
|Quo Vadis                      |{Author II, 1992} |
|In Dubious Battle              |{Author II, 1992} |
|Antic Hay                      |{Author II, 1992} |
|I Know Why the Caged Bird Sings|{Author II, 1992} |
|Dulce et Decorum Est           |{Author II, 1992} |
|The Daffodil Sky               |{Author II, 1992} |
|If Not Now, When?              |{Author II, 1992} |
|The Green Bay Tree             |{Author II, 1992} |
|Consider Phlebas               |{Author II, 1992} |
|That Good Night                |{Author III, 1993}|
|Recalled to Life               |{Author III, 1993}|
|Blood's a Rover                |{Author III, 1993}|
|Pale Kings and Princes         |{Author III, 1993}|
|The Mermaids Singing           |{Author III, 1993}|
|Arms and the Man               |{Author III, 1993}|
|Number the Stars               |{Author III, 1993}|
|Time To Murder And Create      |{Author III, 1993}|
|Unweaving the Rainbow          |{Author III, 1993}|
|The Golden Apples of the Sun   |{Author III, 1993}|
+-------------------------------+------------------+
```

</details>