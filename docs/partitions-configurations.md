# Partitions Configurations
When generating data with the Spark Datafaker Connector, you can control how the output is partitioned by using the following options:

| Option             | Type        | Description                                                                                                           |
|--------------------|-------------|-----------------------------------------------------------------------------------------------------------------------|
| `partitionsColumn` | String      | Name of the column used for partitioning.                                                                             |
| `partitionsCount`  | Int         | Number of partitions to generate. Mutually exclusive with `partitionsValues`.                                         |
| `partitionsValues` | JSON string | Explicit list of values (for primitives, arrays, maps, or structs). Must match the schema type of `partitionsColumn`. |

These options help you define which column to partition by and how many or which partition values to generate.

### partitionsColumn
This option specifies the name of the column used for partitioning the generated data. Partitioning means Spark will group rows by distinct values in this column, which can improve performance and organization, especially for large datasets.

```scala
.option("partitionsColumn", "author")
```
### partitionsCount
When used together with partitionsColumn, this option defines how many distinct partition values to generate for the specified column. The connector will generate that many unique values for the partition column, and distribute rows among these partitions.
```scala
.option("partitionsColumn", "author")
.option("partitionsCount", 3)
```
This will generate 3 distinct authors in the author column, and the dataset rows will be distributed among these three partitions.

### partitionsValues
Instead of generating partition values, you can specify an explicit list of constant partition values. This option requires you to provide a JSON array of values to be used as the distinct partitions for the specified column.
```scala
.option("partitionsColumn", "author")
.option("partitionsValues", """["Marlana Pagac", "Boris Labadie", "Ms. Hung Aufderhar"]""")
```
In this case, the connector will use exactly those three authors as partition values.

## Important Notes
* You must specify partitionsColumn to enable partitioning.
* Use either partitionsCount or partitionsValues — they are mutually exclusive.
* The usage of the pair `partitionsColumn` and `partitionsCount` is consistent across all data types.
* On this page, we cover partitioning options that apply specifically to partition columns with primitive data types, such as `StringType` and `IntegerType`.
>ℹ️ For details on using `partitionsValues` with complex nested types (arrays, maps, structs), please refer to their respective configuration pages:
>- [ArrayType Configuration](./array-type.md)
>- [MapType Configuration](./map-type.md)
>- [StructType Configuration](./struct-type.md)

This setup gives you fine control over how data is grouped and generated in partitions, allowing for efficient downstream processing or simply to simulate real-world partitioned datasets.