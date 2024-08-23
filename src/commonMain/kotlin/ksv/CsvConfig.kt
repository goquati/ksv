package io.github.goquati.ksv

public data class CsvConfig(
    val withHeader: Boolean,
    val withBom: Boolean,
    val delimiter: Char,
    val encoding: Encoding,
) {
    public class Builder(
        public var withHeader: Boolean = true,
        public var withBom: Boolean = false,
        public var delimiter: Char = ',',
        public var encoding: Encoding = Encoding.UTF_8,
    ) {
        internal fun build() = CsvConfig(
            withHeader = withHeader,
            withBom = withBom,
            delimiter = delimiter,
            encoding = encoding,
        )
    }

    public companion object {
        public const val CSV_QUOTE: Char = '"'
        public const val CSV_NEW_LINE: Char = '\n'
        public const val CSV_QUOTE_STR: String = CSV_QUOTE.toString()
        public const val CSV_ESCAPED_QUOTE_STR: String = CSV_QUOTE_STR + CSV_QUOTE_STR
    }
}