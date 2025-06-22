# StructType Configuration
To generate a StructType field, define each nested field using dot notation (fields.STRUCT_FIELD.NESTED_FIELD). This lets you build complex structured data with full control over each sub-field.

Nested structs can contain primitives, arrays, maps, or even other structs—configured in the same way you'd configure top-level fields.
```scala
// Top-level field
.option("fields.title", "#{book.title}")

// Struct fields
.option("fields.author.name", "#{book.author}")
.option("fields.author.birth_year", "#{number.numberBetween '1900', '2000'}")
.option("fields.author.nationalities", """{"expression": "#{address.countryCode}", "count": 2}""")
```
In this example:
* title is a top-level string field.
* author is a struct with:
  * name: a string
  * birth_year: an integer
  * nationalities: an array of two country codes

These options correspond to the following schema:

```scala
val schema = StructType(Seq(
  StructField("title", StringType, nullable = false),
  StructField("author", StructType(Seq(
    StructField("name", StringType),
    StructField("birth_year", IntegerType),
    StructField("nationalities", ArrayType(StringType))
  )), nullable = false)
))
```
> ✅ Tip: Struct field generation works recursively. That means nested structs inside structs can be built by continuing the dot notation like fields.profile.address.city.

> ⚠️ Note: Each nested field must be individually defined using its full path. If any nested field is missing from the configuration, Spark will throw an error during generation.

## Using `partitionsValues` with StructType
When your partition column is a struct (e.g., StructType with named fields), you can define fixed partitions by providing exact struct values using the partitionsValues option.

### Important:
The format of partitionsValues is a JSON string representing an array of JSON-encoded strings — each element of the outer JSON array is itself a JSON string that encodes a struct (essentially a JSON object with named fields).

So `partitionsValues` looks like this:
```json
[
  "{\"name\":\"Cristopher Schinner\",\"birth_year\":\"1927\"}",
  "{\"name\":\"Mrs. Tenesha Hoeger\",\"birth_year\":\"1998\"}",
  "{\"name\":\"Kori Williamson\",\"birth_year\":\"1914\"}"
]
```
Each string here represents a serialized JSON object corresponding to a specific StructType value — one per partition.

Example snippet (simplified):
```scala
.option("partitionsColumn", "author")
.option(
  "partitionsValues",
  """[
    "{\"name\":\"Cristopher Schinner\",\"birth_year\":\"1927\"}",
    "{\"name\":\"Mrs. Tenesha Hoeger\",\"birth_year\":\"1998\"}",
    "{\"name\":\"Kori Williamson\",\"birth_year\":\"1914\"}"
  ]"""
)
```
You can also use the Circe library (used internally by the Spark Datafaker Connector) to generate the properly formatted partitionsValues string:
```scala
val partitionsValues = Seq(
  Map("name" -> "Cristopher Schinner", "birth_year" -> "1927").asJson.noSpaces,
  Map("name" -> "Mrs. Tenesha Hoeger", "birth_year" -> "1998").asJson.noSpaces,
  Map("name" -> "Kori Williamson", "birth_year" -> "1914").asJson.noSpaces
).asJson.noSpaces
```
This defines three fixed partitions, where the author field must match exactly one of the following structs:

**Partition 1:** { "name": "Cristopher Schinner", "birth_year": "1927" }

**Partition 2:** { "name": "Mrs. Tenesha Hoeger", "birth_year": "1998" }

**Partition 3:** { "name": "Kori Williamson", "birth_year": "1914" }

Each generated row will have the author field set to one of these predefined struct values based on its assigned partition.