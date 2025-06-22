# Configuration Guide

This page describes all available options for configuring the Spark Datafaker Connector. You can pass options using `.option(...)` or `.options(...)` when loading data.

---

## Global Options

| Option        | Type           | Default | Description                                                                                                               |
|---------------|----------------|---------|---------------------------------------------------------------------------------------------------------------------------|
| `numRows`     | Int            | `10`    | Total number of rows to generate. Must be a positive number.                                                              |
| `locale`      | String         | `"en"`  | Locale used for generating data (e.g., `en`, `ru`, `de`).                                                                 |
| `silentCheck` | Boolean        | `true`  | If `true`, unsupported data types will be logged and replaced with `StringType`. If `false`, an exception will be thrown. |
| `fields.*`    | String or JSON | -       | Faker expression for the field. Can be a plain expression string or a JSON string with additional options                 |

---

## Field Configuration (`fields.*`)

You can define how each field is generated using the `fields.field_name` option. A field configuration can be either a plain Datafaker expression or a JSON string with additional options.

| Key          | Type     | Description                                               |
|--------------|----------|-----------------------------------------------------------|
| `expression` | String   | Faker expression to use (e.g., `#{name.full_name}`).      |
| `count`      | Int      | Number of elements (for arrays, maps).                    |
| `key`        | String   | Faker expression for map keys. Must be used with `value`. |
| `value`      | String   | Faker expression for map values. Must be used with `key`. |

For more information, see: [Fields Configuration](./fields-configurations.md)

## Partition Configuration

You can control how data is partitioned by specifying one or more partition options.

| Option             | Type        | Description                                                                                                           |
|--------------------|-------------|-----------------------------------------------------------------------------------------------------------------------|
| `partitionsColumn` | String      | Name of the column used for partitioning.                                                                             |
| `partitionsCount`  | Int         | Number of partitions to generate. Mutually exclusive with `partitionsValues`.                                         |
| `partitionsValues` | JSON string | Explicit list of values (for primitives, arrays, maps, or structs). Must match the schema type of `partitionsColumn`. |

For more information, see: [Partitions Configuration](./partitions-configurations.md)