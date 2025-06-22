# Example: Generating a Dataset Partitioned by an ArrayType Column

## Code:

```scala
val spark = SparkSession.builder().master("local[*]").getOrCreate()
spark.sparkContext.setLogLevel("WARN")

import FieldConfig.implicits._

val fields = Map[String, String](
    "title" -> "#{book.title}",
    "translations" -> FieldConfig(expression = "#{address.countryCode}", count = 2),
).map { case (field, expr) => s"fields.$field" -> expr }

val schema = StructType(Seq(
    StructField("title", StringType, nullable = false),
    StructField("translations", ArrayType(StringType), nullable = false),
))


val df = spark
  .read
  .format("datafaker")
  .schema(schema)
  .option("numRows", 10)
  .option("partitionsColumn", "translations")
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
 |-- translations: array (nullable = false)
 |    |-- element: string (containsNull = true)

+-----------------------------+------------+
|title                        |translations|
+-----------------------------+------------+
|Consider the Lilies          |[NO, NF]    |
|A Darkling Plain             |[NO, NF]    |
|Some Buried Caesar           |[NO, NF]    |
|All the King's Men           |[NO, NF]    |
|Consider the Lilies          |[NO, NF]    |
|Some Buried Caesar           |[NO, NF]    |
|The Widening Gyre            |[NO, NF]    |
|Jesting Pilate               |[NO, NF]    |
|Françoise Sagan              |[NO, NF]    |
|A Confederacy of Dunces      |[NO, NF]    |
|Alone on a Wide, Wide Sea    |[VI, SZ]    |
|The Golden Apples of the Sun |[VI, SZ]    |
|Paths of Glory               |[VI, SZ]    |
|The Golden Apples of the Sun |[VI, SZ]    |
|East of Eden                 |[VI, SZ]    |
|A Handful of Dust            |[VI, SZ]    |
|Stranger in a Strange Land   |[VI, SZ]    |
|Bury My Heart at Wounded Knee|[VI, SZ]    |
|Specimen Days                |[VI, SZ]    |
|Postern of Fate              |[VI, SZ]    |
|Blithe Spirit                |[TF, KR]    |
|The Millstone                |[TF, KR]    |
|Waiting for the Barbarians   |[TF, KR]    |
|Where Angels Fear to Tread   |[TF, KR]    |
|Butter In a Lordly Dish      |[TF, KR]    |
|No Highway                   |[TF, KR]    |
|Behold the Man               |[TF, KR]    |
|Paths of Glory               |[TF, KR]    |
|Vanity Fair                  |[TF, KR]    |
|O Jerusalem!                 |[TF, KR]    |
+-----------------------------+------------+
```

</details>