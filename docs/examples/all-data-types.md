# Example: Generating All Supported Data Types
This example demonstrates how to define and generate synthetic data using all major Spark SQL data types, including primitive types (String, Integer, Boolean, etc.), date/time types, and complex types like arrays, structs, and maps. The data is generated using the DataFaker format, and the resulting schema is explicitly declared to match each field.

Use this example to:

* Understand how to represent each data type in a Spark schema

* Learn how to define expressions for fake data generation

* See how nested and complex structures can be composed

## Code:

```scala
val spark = SparkSession.builder().master("local[*]").getOrCreate()
spark.sparkContext.setLogLevel("WARN")

import FieldConfig.implicits._

val fields = Map[String, String](
  // === Primitive Types ===
  // String
  "title" -> "#{book.title}",                                     // StringType
  "description" -> "#{lorem.paragraph}",                          // StringType

  // Numeric
  "edition" -> "#{number.numberBetween '1', '127'}",              // ByteType
  "volume" -> "#{number.numberBetween '1', '32767'}",             // ShortType
  "page" -> "#{number.numberBetween '50', '1000'}",               // IntegerType
  "isbn13" -> "#{number.randomNumber '13', 'false'}",             // LongType
  "price" -> "#{number.randomDouble '2', '1', '500'}",            // FloatType
  "rating" -> "#{number.randomDouble '1', '1', '5'}",             // DoubleType

  // Boolean
  "is_available" -> "#{bool.bool}",                               // BooleanType

  // === Date/Time ===
  "publish_date_old" -> "#{date.birthdayLocalDate '0', '100'}",   // DateType
  "last_updated_old" -> "#{date.past '30', 'DAYS'}",              // TimestampType
  "publish_date" -> "#{timeAndDate.birthday '0', '100'}",         // DateType
  "last_updated" -> "#{timeAndDate.past '30', 'DAYS'}",           // TimestampType
  "timestamp_fixed" -> "2025-05-28 12:30:45",                     // TimestampType

  // === Complex Types ===
  //struct
  "author.name" -> "#{book.author}",
  "author.birth_year" -> "#{number.numberBetween '1900', '2000'}",
  "author.nationalities" -> """{"expression": "#{address.countryCode}", "count": 2}""",

  // Array
  "genres" -> FieldConfig(expression = "#{book.genre}", count = 3),
  "translations" -> FieldConfig(expression = """["#{address.countryCode}", "#{address.countryCode}"]"""),
  "translations_count" -> FieldConfig(expression = "#{address.countryCode}", count = 2),


  // metadata: one Map with key/value arrays as expression strings (not repeated per entry)
  "metadata" -> FieldConfig(
    key = """["awards", "language", "illustrated"]""",
    value = """["#{number.numberBetween '0','5'}", "#{languageCode.iso639}", "#{bool.bool}"]"""
  ),

  // metadata_count: multiple Map entries, key/value generated per entry
  "metadata_count" -> FieldConfig(
    count = 5,
    key = "#{languageCode.iso639}",
    value = "#{number.numberBetween '0','5'}"
  ),

  // metadata_array (MapType with arrays of key/values inside each entry)
  "metadata_array" -> FieldConfig(count = 5),
  "metadata_array.key" -> FieldConfig(expression = "#{languageCode.iso639}", count = 3),
  "metadata_array.value" -> FieldConfig(expression = "#{number.numberBetween '0','5'}", count = 3)
).map { case (field, expr) => s"fields.$field" -> expr }


val schema = StructType(Seq(
  // === Primitive ===
  StructField("title", StringType, nullable = false),
  StructField("description", StringType, nullable = true),

  StructField("edition", ByteType, nullable = false),
  StructField("volume", ShortType, nullable = false),
  StructField("page", IntegerType, nullable = false),
  StructField("isbn13", LongType, nullable = false),
  StructField("price", FloatType, nullable = false),
  StructField("rating", DoubleType, nullable = true),

  StructField("is_available", BooleanType, nullable = false),

  // === Date/Time ===
  StructField("publish_date_old", DateType, nullable = false),
  StructField("last_updated_old", TimestampType, nullable = true),
  StructField("publish_date", DateType, nullable = false),
  StructField("last_updated", TimestampType, nullable = false),
  StructField("timestamp_fixed", TimestampType, nullable = false),

  // === Complex ===
  StructField("genres", ArrayType(StringType), nullable = false),
  StructField("translations", ArrayType(StringType), nullable = false),
  StructField("translations_count", ArrayType(StringType), nullable = false),

  StructField("author", StructType(Seq(
    StructField("name", StringType),
    StructField("birth_year", IntegerType),
    StructField("nationalities", ArrayType(StringType))
  )), nullable = false),

  StructField("metadata", MapType(StringType, StringType), nullable = true),
  StructField("metadata_count", MapType(StringType, StringType), nullable = true),
  StructField("metadata_array", MapType(ArrayType(StringType), ArrayType(StringType)), nullable = true)
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

## Output:

<details>
<summary>See</summary>

```
root
 |-- title: string (nullable = false)
 |-- description: string (nullable = true)
 |-- edition: byte (nullable = false)
 |-- volume: short (nullable = false)
 |-- page: integer (nullable = false)
 |-- isbn13: long (nullable = false)
 |-- price: float (nullable = false)
 |-- rating: double (nullable = true)
 |-- is_available: boolean (nullable = false)
 |-- publish_date_old: date (nullable = false)
 |-- last_updated_old: timestamp (nullable = true)
 |-- publish_date: date (nullable = false)
 |-- last_updated: timestamp (nullable = false)
 |-- timestamp_fixed: timestamp (nullable = false)
 |-- genres: array (nullable = false)
 |    |-- element: string (containsNull = true)
 |-- translations: array (nullable = false)
 |    |-- element: string (containsNull = true)
 |-- translations_count: array (nullable = false)
 |    |-- element: string (containsNull = true)
 |-- author: struct (nullable = false)
 |    |-- name: string (nullable = true)
 |    |-- birth_year: integer (nullable = true)
 |    |-- nationalities: array (nullable = true)
 |    |    |-- element: string (containsNull = true)
 |-- metadata: map (nullable = true)
 |    |-- key: string
 |    |-- value: string (valueContainsNull = true)
 |-- metadata_count: map (nullable = true)
 |    |-- key: string
 |    |-- value: string (valueContainsNull = true)
 |-- metadata_array: map (nullable = true)
 |    |-- key: array
 |    |    |-- element: string (containsNull = true)
 |    |-- value: array (valueContainsNull = true)
 |    |    |-- element: string (containsNull = true)

+-------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------+------+----+-------------+------+------+------------+----------------+-----------------------+------------+-----------------------+-------------------+--------------------------------------------------------+------------+------------------+-------------------------------------+---------------------------------------------------+---------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------+
|title                    |description                                                                                                                                                                                                                                                                                     |edition|volume|page|isbn13       |price |rating|is_available|publish_date_old|last_updated_old       |publish_date|last_updated           |timestamp_fixed    |genres                                                  |translations|translations_count|author                               |metadata                                           |metadata_count                               |metadata_array                                                                                                                         |
+-------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------+------+----+-------------+------+------+------------+----------------+-----------------------+------------+-----------------------+-------------------+--------------------------------------------------------+------------+------------------+-------------------------------------+---------------------------------------------------+---------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------+
|To Say Nothing of the Dog|Officiis recusandae occaecati saepe eos. Optio commodi neque corporis dolor dolores. Architecto ipsa provident pariatur. Dolore id eos neque nemo necessitatibus.                                                                                                                               |16     |704   |378 |2757166332544|265.45|3.8   |false       |1934-06-06      |2025-06-16 13:13:35.792|1946-07-20  |2025-06-06 16:52:50.387|2025-05-28 12:30:45|[Western, Fantasy, Humor]                               |[BN, AE]    |[GD, CY]          |{Missy Thiel, 1916, [BY, RO]}        |{awards -> 1, language -> nn, illustrated -> false}|{gl -> 2, zu -> 3, sq -> 1, mr -> 4, ar -> 0}|{[av, fj, af] -> [1, 2, 3], [yo, kn, as] -> [2, 1, 1], [rw, bn, la] -> [2, 2, 3], [ga, ba, jv] -> [4, 3, 2], [bm, rw, ny] -> [4, 1, 4]}|
|Quo Vadis                |Iusto explicabo fugit unde. Soluta mollitia recusandae dolores nam. Nisi magni harum explicabo ut.                                                                                                                                                                                              |52     |32269 |933 |7869814313650|281.6 |2.7   |true        |1935-12-24      |2025-05-22 15:29:26.014|1963-07-15  |2025-06-11 10:36:01.633|2025-05-28 12:30:45|[Fiction in verse, Reference book, Essay]               |[CL, BO]    |[IO, CH]          |{Miss Rochell Green, 1962, [WS, NA]} |{awards -> 1, language -> ik, illustrated -> true} |{ar -> 2, oj -> 4, nb -> 0, to -> 3, dv -> 2}|{[lt, mi, fr] -> [3, 0, 1], [de, et, ii] -> [1, 2, 3], [en, eu, kn] -> [1, 4, 2], [my, sc, es] -> [3, 1, 0], [sc, es, ka] -> [4, 0, 2]}|
|The Millstone            |Ipsum porro nostrum dignissimos illum hic soluta veritatis. Quisquam odio illum deserunt occaecati inventore quisquam autem. Possimus atque non voluptatibus accusantium veritatis quaerat dolore.                                                                                              |126    |24768 |500 |3323158304889|243.33|3.0   |false       |1927-10-15      |2025-06-08 10:43:45.167|1926-07-28  |2025-05-29 16:52:22.1  |2025-05-28 12:30:45|[Reference book, Essay, Reference book]                 |[TN, SY]    |[GW, AR]          |{Raleigh Koelpin DDS, 1950, [YE, GB]}|{awards -> 3, language -> da, illustrated -> true} |{uk -> 2, be -> 0, fi -> 0, nb -> 2, as -> 2}|{[na, lb, eu] -> [1, 0, 2], [da, tw, ee] -> [0, 2, 3], [sd, qu, pa] -> [1, 4, 4], [nr, ur, gn] -> [3, 4, 4], [nr, ka, qu] -> [0, 4, 4]}|
|A Confederacy of Dunces  |Quasi sequi reprehenderit fugiat tempora dicta omnis quis. Odio maxime veritatis omnis ad beatae vitae quod. Est assumenda quos aut deserunt blanditiis repudiandae. Ratione corporis impedit. Qui doloribus quas dolor dolore quam error culpa.                                                |1      |2337  |261 |3689606288079|245.08|1.7   |true        |1960-07-23      |2025-05-26 02:41:31.243|1971-08-27  |2025-06-06 20:29:22.048|2025-05-28 12:30:45|[Fantasy, Narrative nonfiction, Fanfiction]             |[PL, AS]    |[BS, ER]          |{Jessie Yost II, 1976, [FR, SG]}     |{awards -> 4, language -> ae, illustrated -> false}|{oj -> 1, vi -> 1, no -> 4, lb -> 1, sa -> 2}|{[ia, ab, cy] -> [1, 3, 1], [dz, sl, ee] -> [4, 3, 2], [ik, my, jv] -> [1, 3, 1], [tg, ko, eu] -> [0, 0, 1], [bm, ku, mg] -> [4, 4, 3]}|
|Look to Windward         |Eum beatae voluptas. Consequuntur esse aliquid sint dignissimos unde. Aliquid fugiat recusandae quo. Atque libero ullam ab nam deserunt.                                                                                                                                                        |21     |16687 |249 |8358542628718|93.49 |1.2   |false       |1989-03-21      |2025-05-23 19:47:21.843|1964-03-21  |2025-06-03 07:57:24.381|2025-05-28 12:30:45|[Folklore, Legend, Fairy tale]                          |[CL, BF]    |[MY, GP]          |{Rocky Gaylord Sr., 1911, [US, SO]}  |{awards -> 3, language -> az, illustrated -> false}|{kr -> 3, et -> 0, ca -> 1, ml -> 3, is -> 3}|{[it, eo, dz] -> [2, 3, 0], [dv, wa, vi] -> [2, 1, 3], [pa, et, ms] -> [4, 4, 1], [av, br, in] -> [0, 3, 2], [gl, bg, co] -> [1, 3, 1]}|
|Postern of Fate          |Ipsam odit dolorum tempora autem. Cupiditate dolorem doloribus nobis sed. Consectetur provident neque vel eum corporis reiciendis. Impedit eaque esse sequi nemo consequuntur sed laboriosam. Velit quod aliquid suscipit tenetur officiis sequi.                                               |96     |22964 |100 |2272488779773|308.65|3.6   |false       |1932-01-06      |2025-06-09 07:36:14.133|1960-12-09  |2025-06-01 11:07:28.887|2025-05-28 12:30:45|[Mythology, Fable, Tall tale]                           |[AD, KP]    |[BE, MQ]          |{Carie Gusikowski, 1955, [MZ, TT]}   |{awards -> 2, language -> en, illustrated -> false}|{cu -> 3, ar -> 1, kw -> 0, ku -> 4, kn -> 1}|{[my, kk, my] -> [4, 4, 1], [ch, zh, to] -> [3, 0, 2], [si, dz, iw] -> [2, 2, 0], [co, so, bm] -> [1, 1, 3], [ga, fj, ng] -> [2, 4, 2]}|
|Pale Kings and Princes   |Excepturi accusamus eligendi dolorem repudiandae aliquid reprehenderit. Voluptas quam inventore minus aperiam impedit. Eum cumque praesentium. Non asperiores at.                                                                                                                               |116    |20195 |307 |8367436621526|26.95 |3.4   |true        |2013-06-12      |2025-05-25 19:53:20.972|1988-10-03  |2025-06-06 13:58:35.155|2025-05-28 12:30:45|[Folklore, Short story, Folklore]                       |[TN, BT]    |[ZA, BB]          |{Marlena Parker, 1992, [EC, GQ]}     |{awards -> 2, language -> ie, illustrated -> true} |{hr -> 3, nn -> 1, qu -> 3, ab -> 1, ar -> 1}|{[nl, vo, st] -> [0, 0, 1], [az, az, to] -> [3, 4, 0], [ve, qu, mt] -> [1, 3, 4], [uk, lt, sk] -> [3, 0, 2], [ug, el, sq] -> [3, 4, 4]}|
|Tender Is the Night      |Ab veniam commodi odit nam doloribus dicta labore. Hic minus mollitia. Nulla libero dignissimos voluptatibus veniam modi.                                                                                                                                                                       |72     |1813  |637 |1443758296498|151.58|4.2   |false       |1979-05-18      |2025-05-19 11:35:15.857|1975-05-06  |2025-06-12 18:04:24.433|2025-05-28 12:30:45|[Historical fiction, Legend, Speech]                    |[GE, HN]    |[MK, PT]          |{Melynda Hegmann I, 1934, [RS, MG]}  |{awards -> 4, language -> no, illustrated -> false}|{ii -> 3, qu -> 4, ja -> 0, li -> 1, bh -> 0}|{[bo, mn, yi] -> [4, 4, 4], [to, ps, zh] -> [3, 0, 3], [ar, ti, ff] -> [3, 2, 4], [ks, kr, bg] -> [1, 0, 3], [bo, sn, bm] -> [4, 0, 0]}|
|Arms and the Man         |Iure magni mollitia fugiat nulla ex dolorum provident. Accusantium enim natus consectetur. Reprehenderit voluptatibus exercitationem optio nobis doloremque nesciunt modi.                                                                                                                      |104    |18047 |281 |2038619024159|310.08|3.7   |false       |1967-10-24      |2025-06-16 03:30:24.801|1954-10-26  |2025-06-10 03:17:34.391|2025-05-28 12:30:45|[Historical fiction, Science fiction, Realistic fiction]|[HU, TF]    |[NF, AM]          |{Miss Soon Koepp, 1939, [IQ, BB]}    |{awards -> 2, language -> kl, illustrated -> true} |{fa -> 1, sv -> 1, pa -> 2, tr -> 2, en -> 2}|{[tn, pa, li] -> [1, 3, 4], [yi, co, ig] -> [2, 1, 2], [kl, tt, fi] -> [1, 1, 0], [yi, jv, ae] -> [4, 4, 0], [ce, sk, li] -> [0, 4, 2]}|
|To a God Unknown         |Odit magni mollitia eveniet voluptatibus dolorum magni vero. Non impedit suscipit odit accusantium impedit nesciunt perspiciatis. Mollitia earum adipisci magnam reiciendis nulla iure. Magnam maxime atque nostrum. Dolorum mollitia dolores explicabo provident voluptas culpa exercitationem.|1      |1703  |782 |5782357008966|224.14|5.0   |false       |1985-10-14      |2025-05-22 10:18:07.249|2020-05-20  |2025-06-02 16:51:54.005|2025-05-28 12:30:45|[Reference book, Speech, Mythology]                     |[SV, BZ]    |[GA, BQ]          |{Eric Klocko, 1918, [BJ, BM]}        |{awards -> 1, language -> si, illustrated -> true} |{ji -> 3, so -> 4, ay -> 0, ps -> 4, io -> 4}|{[ca, lb, lb] -> [3, 4, 3], [om, la, kk] -> [4, 3, 1], [nd, om, mt] -> [4, 4, 1], [ms, bo, bh] -> [4, 1, 1], [ks, qu, ss] -> [0, 2, 0]}|
+-------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------+------+----+-------------+------+------+------------+----------------+-----------------------+------------+-----------------------+-------------------+--------------------------------------------------------+------------+------------------+-------------------------------------+---------------------------------------------------+---------------------------------------------+---------------------------------------------------------------------------------------------------------------------------------------+
```

</details>