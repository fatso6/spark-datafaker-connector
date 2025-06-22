# Supported Data Types
The Spark Datafaker Connector supports both primitive and complex data types in schema definitions. You can use these types in any field, and combine them freely to build custom nested schemas.

## Primitive Types
The following primitive types are fully supported:

| Type            | Description                       | Example                                             |
|-----------------|-----------------------------------|-----------------------------------------------------|
| `StringType`    | UTF-8 encoded string              | `"hello"`                                           |
| `ByteType`      | 8-bit signed integer              | `123`                                               |
| `ShortType`     | 16-bit signed integer             | `12345`                                             |
| `IntegerType`   | 32-bit signed integer             | `100000`                                            |
| `LongType`      | 64-bit signed integer             | `12345678900`                                       |
| `FloatType`     | 32-bit floating-point number      | `3.14`                                              |
| `DoubleType`    | 64-bit floating-point number      | `123456.789`                                        |
| `BooleanType`   | Boolean value                     | `true` / `false`                                    |
| `DateType`      | Calendar date (no time component) | `"2023-10-15"`                                      |
| `TimestampType` | Date and time                     | `"2023-10-15T08:30:00Z"` or `"2023-10-15 08:30:00"` |


## Date and Timestamp Format Support
String values for DateType and TimestampType must follow specific formats:

| Format                       | Description         |
|------------------------------|---------------------|
| `yyyy-MM-dd`                 | Local date          |
| `yyyy-MM-ddTHH:mm:ss[.SSS]Z` | ISO timestamp (UTC) |
| `yyyy-MM-dd HH:mm:ss[.SSS]`  | SQL-style timestamp |


If a value does not match one of the supported formats, a parsing error will be thrown.

## Complex Types
You can use the following complex types in any field (including deeply nested structures):

| Type                           | Description                                          |
|--------------------------------|------------------------------------------------------|
| [`ArrayType`](array-type.md)   | Ordered list of elements of a supported type         |
| [`MapType`](map-type.md)       | Key-value pairs with supported key/value types       |
| [`StructType`](struct-type.md) | Named collection of typed fields (like a case class) |


These types behave like regular fields: they can be generated randomly, nested, or structured using Datafaker templates.

> For more details, see:
> 
> [Array Type Configuration](array-type.md)
> 
> [Map Type Configuration](map-type.md)
> 
> [Struct Type Configuration](struct-type.md)