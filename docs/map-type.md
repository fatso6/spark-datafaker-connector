# Map Type Configuration
To generate a `MapType` field, use the `key`, `value`, and `count` keys in the JSON field configuration. You can also provide arrays of expressions for keys and values if each pair should be generated differently.

## Modes
MapType supports two modes:
1. Fixed count of key-value pairs
2. Arrays of key and value expressions

### 1. Fixed Count of Key-Value Pairs
Use the key, value, and count keys to generate a map with the specified number of entries. Each key and value will be generated using the same expressions.

```scala
// JSON string
.option(
  "fields.metadata",
  """{
    "key": "#{languageCode.iso639}",
    "value": "#{number.numberBetween '0','5'}",
    "count": 5
  }"""
)

// FieldConfig
import FieldConfig.implicits._
.option(
  "fields.metadata",
  FieldConfig(
    key = "#{languageCode.iso639}",
    value = "#{number.numberBetween '0','5'}",
    count = 5
  )
)
```

### 2. Arrays of Key and Value Expressions
Provide arrays of expressions for both key and value if you want each key-value pair to be generated using a different expression.

```scala
// JSON string
.option(
  "fields.metadata",
  """{
    "key": ["#{languageCode.iso639}", "#{locale}"],
    "value": ["#{number.numberBetween '0','5'}", "#{number.digit}"]
  }"""
)

// FieldConfig
import FieldConfig.implicits._
.option(
  "fields.metadata",
  FieldConfig(
    key = """["#{languageCode.iso639}", "#{locale}"]""",
    value = """["#{number.numberBetween '0','5'}", "#{number.digit}"]"""
  )
)
```

## Nested Complex Types
If a map contains complex types (e.g., arrays, structs, or other maps) as its keys or values, you need to use the .key and .value sub-fields to define how each part is generated.

This gives you fine-grained control over deeply nested structures like `Map[String, Array[Int]]`.

```scala
.option("fields.metadata_array", FieldConfig(count = 5)) // Number of map entries
.option("fields.metadata_array.key", FieldConfig(expression = "#{languageCode.iso639}", count = 5)) // Map keys
.option("fields.metadata_array.value", FieldConfig(expression = "#{number.numberBetween '0','5'}", count = 5)) // Map values (arrays of numbers)
```
In this example:
`fields.metadata_array` is a map where:
* Each key is a language code.
* Each value is an array of 5 random numbers between 0 and 5.

> ⚠️ Important: When generating complex maps, you must set count for both key and value, and the values must be equal.
This ensures that keys and values align properly and the map has consistent entry pairs.

The same structure applies when keys or values are nested structs or maps themselves.

## Using `partitionsValues` with MapType
When your partition column is a map (e.g., `MapType(StringType, IntegerType))`, you can define fixed partitions by specifying exact map values using the partitionsValues option.

### Important:
The format of `partitionsValues` is a JSON string representing an array of JSON-encoded strings — meaning each element inside the outer JSON array is itself a JSON string that encodes a map.

So `partitionsValues` looks like this:
```json
[
  "{\"Rosenda Reilly\":\"1995\",\"Miss April Reinger\":\"1994\"}",
  "{\"Eloisa Simonis III\":\"1998\",\"Tyree Kreiger\":\"1975\"}",
  "{\"Imogene Hickle Jr.\":\"1988\",\"Mr. Leana Watsica\":\"1971\"}"
]
```
Each string here is a serialized JSON object, representing a single partition's map value.

Example snippet (simplified):
```scala
.option("partitionsColumn", "author")
.option(
  "partitionsValues",
  """[
    "{\"Rosenda Reilly\":\"1995\",\"Miss April Reinger\":\"1994\"}",
    "{\"Eloisa Simonis III\":\"1998\",\"Tyree Kreiger\":\"1975\"}",
    "{\"Imogene Hickle Jr.\":\"1988\",\"Mr. Leana Watsica\":\"1971\"}"
  ]"""
)
```
Alternatively, you can also use the Circe library (which is used internally by the Spark Datafaker Connector) to build the correct `partitionsValues` string:
```scala
val partitionsValues = Seq(
  Map("Rosenda Reilly" -> "1995", "Miss April Reinger" -> "1994").asJson.noSpaces,
  Map("Eloisa Simonis III" -> "1998", "Tyree Kreiger" -> "1975").asJson.noSpaces,
  Map("Imogene Hickle Jr." -> "1988", "Mr. Leana Watsica" -> "1971").asJson.noSpaces
).asJson.noSpaces
```

This creates three fixed partitions, where the author field in each row will match exactly one of the specified maps:

**Partition 1:** {"Rosenda Reilly":"1995", "Miss April Reinger":"1994"}

**Partition 2:** {"Eloisa Simonis III":"1998", "Tyree Kreiger":"1975"}

**Partition 3:** {"Imogene Hickle Jr.":"1988", "Mr. Leana Watsica":"1971"}

Each row will be generated with the author map matching exactly one of these predefined values, depending on the partition it belongs to.

