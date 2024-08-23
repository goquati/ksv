# KSV - Kotlin CSV Serialization Library

![GitHub License](https://img.shields.io/github/license/goquati/ksv)
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/goquati/ksv/check.yml)
![Static Badge](https://img.shields.io/badge/coverage-100%25-success)

**KSV** is a Kotlin library designed to provide a type-safe and convenient way to write CSV files. It leverages Kotlin's coroutines and flows, making it a perfect choice for handling large datasets in a non-blocking manner. Although currently limited to JVM platforms, KSV is built with multi-platform support in mind. Future updates are planned to include CSV reading capabilities.

## Features

- **Type-safe CSV Writing:** Define your CSV schema using Kotlin's type-safe syntax.
- **Coroutines/Flows Support:** Seamlessly integrate with Kotlin flows for efficient, asynchronous data processing.
- **Customizable Output:** Easily configure delimiters, headers, and format columns to match your needs.
- **Multi-platform Ready:** Designed with future multi-platform support in mind, though currently available only for JVM.

## Installation

To use KSV in your project, add the following dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.goquati:ksv:$VERSION")
}
```

## Usage

### Defining Your Data

To start, define the data class that represents the rows in your CSV file:

```kotlin
data class RowData(
    val title: String,
    val amount: Double,
    val description: String?,
)
```

### Writing CSV Files

You can serialize a flow of your data class instances to a CSV format. The following example demonstrates how to use KSV to serialize a flow of `RowData`:

```kotlin
val rows = flowOf(
    RowData(title = "Foobar", amount = 3.0, description = null),
    RowData(title = "Hello", amount = -47.0, description = "world"),
)

val csvStream: Flow<String> = rows.serializeCsv {
    config {
        delimiter = ';'
        withHeader = true
    }
    schema {
        column(RowData::title) // Use property name as header
        column(name = "amount") { String.format("%.02f", amount.absoluteValue) } // Format amount with 2 decimals
        column(name = "sign") { if (amount < 0) "+" else "-" } // Add sign column based on amount
        column(name = "info") { description ?: "no-info" } // Handle null description
    }
}
```

This code will produce a CSV with the following format:

```csv
title;amount;sign;info
Foobar;3.00;-;no-info
Hello;47.00;+;world
```

### Configuration Options

You can customize various aspects of the CSV output:

- **Delimiter:** Change the default delimiter from `,` to any character, e.g., `;`.
- **Header:** Toggle whether the CSV should include a header row.
- **Schema:** Define how each property of your data class should be serialized into the CSV columns.

### Planned Features

- **CSV Reading:** Future releases will include functionality to read CSV files into type-safe Kotlin data classes.
- **Extended Platform Support:** Expanding support to other Kotlin platforms, including JS and Native.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

With KSV, you can efficiently write CSV files in a Kotlin-idiomatic way, leveraging the power of coroutines and type safety. Start using it in your projects today and stay tuned for future updates!