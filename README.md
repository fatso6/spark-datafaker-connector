# 🧪 Spark DataFaker Connector

![Maven Central Version](https://img.shields.io/maven-central/v/io.github.aalopatin/aalopatin-test)
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/aalopatin/spark-datafaker-connector/master-publish.yml)
![GitHub License](https://img.shields.io/github/license/aalopatin/spark-datafaker-connector)

Generate realistic, structured test data for Apache Spark using DataFaker expression syntax.

This connector is designed to quickly fill DataFrames with random but realistic data for testing pipelines, demoing schemas, or mocking datasets — all without needing external data sources.

---

## ✨ Features

- Support for primitive types (`String`, `Int`, `Float`, `Boolean`, etc.)
- Date & Timestamp generation
- Nested structures (`StructType`, `ArrayType`, `MapType`)
- Partition generation
- Schema-first approach: you define the schema, we fill it

---

## 🚀 Quick Example

```scala
val spark = SparkSession.builder().master("local[*]").getOrCreate()

import FieldConfig.implicits._

val fields = Map[String, String](
  "title" -> "#{book.title}",
  "page_count" -> "#{number.numberBetween '50', '1000'}",
  "isbn13" -> "#{number.randomNumber '13', 'false'}",
  "is_available" -> "#{bool.bool}",
  "publish_date" -> "#{timeAndDate.birthday '0', '100'}",
  "last_updated" -> "#{timeAndDate.past '30', 'DAYS'}",
  "author.name" -> "#{book.author}",
  "author.birth_year" -> "#{number.numberBetween '1900', '2000'}",
  "author.nationalities" -> """{"expression": "#{address.countryCode}", "count": 2}""",
  "genres" -> FieldConfig(expression = "#{book.genre}", count = 3),
  "metadata" -> FieldConfig(
    key = """["awards", "language", "illustrated"]""",
    value = """["#{number.numberBetween '0','5'}", "#{languageCode.iso639}", "#{bool.bool}"]"""
  )
).map { case (field, expr) => s"fields.$field" -> expr }


val schema = StructType(Seq(
  StructField("title", StringType, nullable = false),
  StructField("page_count", IntegerType, nullable = false),
  StructField("isbn13", LongType, nullable = false),
  StructField("is_available", BooleanType, nullable = false),
  StructField("publish_date", DateType, nullable = false),
  StructField("last_updated", TimestampType, nullable = false),
  StructField("genres", ArrayType(StringType), nullable = false),
  StructField("author", StructType(Seq(
    StructField("name", StringType),
    StructField("birth_year", IntegerType),
    StructField("nationalities", ArrayType(StringType))
  )), nullable = false),

  StructField("metadata", MapType(StringType, StringType), nullable = true)
))

val df = spark
  .read
  .format("datafaker")
  .schema(schema)
  .option("numRows", 10)
  .options(fields)
  .load()

df.printSchema()
df.show(10, truncate = false)
```
☝️ This creates a DataFrame with 10 rows of fully synthetic, schema-compliant data.

| title                    | page_count | isbn13        | is_available | publish_date | last_updated            | genres                                         | author                              | metadata                                            |
|--------------------------|------------|---------------|--------------|--------------|-------------------------|------------------------------------------------|-------------------------------------|-----------------------------------------------------|
| Lilies of the Field      | 905        | 1009627837203 | false        | 1977-12-19   | 2025-06-03 11:00:12.098 | [Metafiction, Science fiction, Mystery]        | {Eloy Reichel, 1939, [OM, NO]}      | {awards -> 2, language -> iu, illustrated -> false} |
| The Daffodil Sky         | 158        | 8035331433389 | false        | 1930-07-31   | 2025-06-04 19:15:38.604 | [Mythopoeia, Humor, Comic/Graphic Novel]       | {Theola Mohr, 1936, [MW, BM]}       | {awards -> 0, language -> ba, illustrated -> false} |
| The Sun Also Rises       | 344        | 5492886207418 | false        | 1969-07-29   | 2025-05-29 09:46:52.231 | [Fantasy, Mythology, Western]                  | {Neville Abshire, 1998, [WF, UY]}   | {awards -> 1, language -> mn, illustrated -> false} |
| Dulce et Decorum Est     | 77         | 4435180189852 | true         | 1981-07-04   | 2025-06-15 12:38:56.745 | [Fairy tale, Biography/Autobiography, Western] | {Jeffery Corwin, 1955, [TZ, ML]}    | {awards -> 4, language -> bn, illustrated -> false} |
| Some Buried Caesar       | 324        | 8939347566217 | true         | 2002-08-16   | 2025-05-31 10:27:50.982 | [Mythopoeia, Mythology, Metafiction]           | {Jenelle Emmerich, 1927, [GY, ML]}  | {awards -> 2, language -> my, illustrated -> true}  |
| A Many-Splendoured Thing | 285        | 6077413634485 | false        | 1927-07-19   | 2025-06-12 05:50:55.576 | [Western, Fanfiction, Classic]                 | {Dr. Bree Rempel, 1925, [HK, ZA]}   | {awards -> 2, language -> eu, illustrated -> true}  |
| Specimen Days            | 476        | 3195536084630 | true         | 1986-11-01   | 2025-05-24 10:18:04.756 | [Tall tale, Metafiction, Science fiction]      | {Marguerite Wunsch, 1992, [GR, TR]} | {awards -> 1, language -> wa, illustrated -> false} |
| His Dark Materials       | 906        | 1177451685597 | true         | 2013-01-03   | 2025-05-27 11:04:59.104 | [Suspense/Thriller, Metafiction, Essay]        | {Olen Gutkowski, 1929, [NE, TC]}    | {awards -> 3, language -> ka, illustrated -> false} |
| From Here to Eternity    | 667        | 5668190173476 | true         | 2012-08-23   | 2025-06-11 21:27:12.114 | [Fairy tale, Science fiction, Fanfiction]      | {Walker McDermott, 1911, [NA, BY]}  | {awards -> 4, language -> th, illustrated -> false} |
| From Here to Eternity    | 658        | 1908920136208 | true         | 1950-10-21   | 2025-06-06 08:48:20.357 | [Humor, Reference book, Textbook]              | {Elanor Little, 1983, [BN, SL]}     | {awards -> 0, language -> sm, illustrated -> true}  |

## 📦 Installation
### 1. Add the Spark DataFaker Connector
Maven:
```xml
<dependency>
  <groupId>io.github.aalopatin</groupId>
  <artifactId>spark-datafaker-connector</artifactId>
  <version>0.1.0</version>
</dependency>
```

SBT:
```scala
libraryDependencies += "io.github.aalopatin" % "spark-datafaker-connector" % "1.0.0"
```

### 2. Add the DataFaker Dependency
You must also explicitly include the DataFaker library. The connector delegates expression evaluation to this library.

Maven:
```xml
<dependency>
  <groupId>net.datafaker</groupId>
  <artifactId>datafaker</artifactId>
  <version>2.4.2</version>
</dependency>
```
SBT:
```scala
libraryDependencies += "net.datafaker" % "datafaker" % "2.4.2"
```

> 💡 The connector requires only support for DataFaker expressions, so it should work with most versions of DataFaker that support them. It has been tested with versions 1.9.0 and above, including 2.x. You’re free to choose the version that best fits your environment.

### 3. Add Circe Dependencies
The connector uses Circe internally for JSON parsing, but does not bundle it. You must provide compatible Circe modules in your Spark application.

Maven:
```xml
<dependecies>
    <dependency>
      <groupId>io.circe</groupId>
      <artifactId>circe-parser_2.13</artifactId>
      <version>0.14.10</version>
    </dependency>
    
    <dependency>
        <groupId>io.circe</groupId>
        <artifactId>circe-generic-extras_2.13</artifactId>
        <version>0.14.4</version>
    </dependency>
</dependecies>
```

SBT:
```scala
libraryDependencies += "io.circe" %% "circe-parser" % "0.14.10"
libraryDependencies += "io.circe" %% "circe-generic-extras" % "0.14.4"
```

> ⚠️ These dependencies are marked as provided in the connector’s pom.xml to avoid bundling them in the final JAR. This keeps the artifact lightweight and avoids potential version conflicts in your application.

## 📚 Configuration and Examples

For advanced usage, custom field mappings, and real-world examples, check out the full [configuration guide](docs/configurations.md) and [examples](docs/examples/) in the [/docs](docs) directory.