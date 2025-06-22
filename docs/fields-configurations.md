# Field Configuration

This page provides an in-depth explanation of how to configure individual fields when using the Spark Datafaker Connector. You can define each field using the `fields.field_name` syntax.

The connector supports multiple formats for specifying how data should be generated for each field.

---

## 1. Simple Expression

You can directly assign a Datafaker expression as a string.

```scala
.option("fields.name", "#{name.full_name}")
```
This is the most concise form and is ideal for primitive types.

## 2. JSON String (with Additional Options)

For more advanced control — such as generating arrays or maps — you can use a JSON string with the following keys:

| Key          | Type     | Description                                               |
|--------------|----------|-----------------------------------------------------------|
| `expression` | String   | Faker expression to use (e.g., `#{name.full_name}`).      |
| `count`      | Int      | Number of elements (for arrays, maps).                    |
| `key`        | String   | Faker expression for map keys. Must be used with `value`. |
| `value`      | String   | Faker expression for map values. Must be used with `key`. |

You can write the JSON string manually:

```scala
// Array
.option("fields.genres", """{"expression": "#{book.genre}", "count": 3}""")

//Map
.option("fields.metadata", """{"key":"#{languageCode.iso639}","value":"#{number.numberBetween '0','5'}","count":5}""")
```

Or use the FieldConfig class to generate the JSON string automatically. For implicit conversion into string import `FieldConfig.implicits._` 

```scala
import FieldConfig.implicits._

// Array
.option("fields.genres", FieldConfig(expression = "#{book.genre}", count = 3))

// Map
.option(
  "metadata",
  FieldConfig(
    key = "#{languageCode.iso639}",
    value = "#{number.numberBetween '0','5'}",
    count = 5
  )
)
```

>ℹ️ For advanced usage with nested and complex types, see:
>- [ArrayType Configuration](./array-type.md)
>- [MapType Configuration](./map-type.md)
>- [StructType Configuration](./struct-type.md)

