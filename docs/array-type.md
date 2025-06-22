# Array Type Configuration
To generate an `ArrayType` field, use the `count` key in the JSON field configuration, or provide an array of expressions in the `expression` key.

## Modes
`ArrayType` supports two modes:
1. Fixed count of values
2. Array of different expressions

### 1. Fixed Count of Values

Use the `count` key to generate an array with the specified number of elements, all based on the same expression.

```scala
// JSON string
.option("fields.genres", """{"expression": "#{book.genre}", "count": 3}""")

//FieldConfig
import FieldConfig.implicits._
.option("fields.genres", FieldConfig(expression = "#{book.genre}", count = 3))
```

### 2. Array of Different Expressions

Provide an array of expressions in the `expression` key to generate an array where each element is generated using a different expression.

```scala
// JSON string
.option("fields.translations", """{"expression": ["#{address.countryCode}", "#{languageCode.iso639}"]}""")

//FieldConfig
.option("fields.translations", FieldConfig(expression = """["#{address.countryCode}", "#{languageCode.iso639}"]"""))
```

## Nested Complex Types
If the array contains complex types (e.g., another array, struct, or map), you need to use the .value sub-field to define the configuration for each element inside the array.

This lets you recursively describe how each nested level should be generated.

```scala
.option("fields.matrix", FieldConfig(count = 3)) // Outer array
.option("fields.matrix.value", FieldConfig(expression = "#{number.numberBetween '1', '10'}", count = 5)) // Inner array
```

In this example:

`fields.matrix` is a 2D array, where:
* The outer array contains 3 elements.
* Each inner array contains 5 random numbers between 1 and 10.

> ⚠️ Note: Always use the `.value` suffix when configuring nested arrays or complex elements. The same pattern applies to arrays of structs and maps as well.

## Using `partitionsValues` with ArrayType
When your partition column is an array (e.g., `ArrayType(StringType)`), you can specify fixed partitions by providing the exact array values for each partition using the `partitionsValues` option.

### Important:
The format of `partitionsValues` is a JSON string representing an array of JSON-encoded strings — meaning each element inside the main JSON array is itself a JSON string that encodes an array.

In other words, `partitionsValues` looks like this:

```json
[
  "[\"GG\", \"PE\"]",
  "[\"SK\", \"UM\"]",
  "[\"GH\", \"SV\"]"
]
```
Each string here represents a serialized JSON array corresponding to one partition value.

### Example snippet (simplified):
```scala
.option("partitionsColumn", "translations")
.option(
  "partitionsValues", 
  """[
    "[\"GG\", \"PE\"]",
    "[\"SK\", \"UM\"]",
    "[\"GH\", \"SV\"]"
  ]"""
)
```

Alternatively, you can use the Circe library (which is used by the Spark Datafaker Connector for parsing configurations and other internal processing) to create the correctly formatted JSON string for the `partitionsValues` configuration:

```scala
val partitionsValues = Seq(
  Seq("GG", "PE").asJson.noSpaces,
  Seq("SK", "UM").asJson.noSpaces,
  Seq("GH", "SV").asJson.noSpaces
).asJson.noSpaces
```

This means you want to create exactly three partitions, where the partition column `translations` has these exact array values for each partition:

**Partition 1:** ["GG", "PE"]

**Partition 2:** ["SK", "UM"]

**Partition 3:** ["GH", "SV"]

Each row generated in the dataset will have its `translations` field equal to one of these arrays, depending on the partition it belongs to.