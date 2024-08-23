package io.github.goquati.ksv

public data class CsvColumn<Row>(
    val name: String,
    val forceEscape: Boolean,
    val row2Cell: Row.() -> String,
)
