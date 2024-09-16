package io.github.goquati.ksv

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

public data class CsvSerializer<Row>(
    val config: CsvConfig,
    val schema: CsvSchema<Row>,
) {
    public fun streamCsv(data: Flow<Row>): Flow<String> = flow {
        if (config.withBom) emit(config.encoding.bom)
        if (config.withHeader) {
            emitRow { it.name.csvEscape(forceEscape = false) }
            emit(CsvConfig.CSV_NEW_LINE.toString())
        }
        data.collect { row ->
            emitRow { it.convert(row) }
            emit(CsvConfig.CSV_NEW_LINE.toString())
        }
    }

    private fun CsvColumn<Row>.convert(row: Row) = row.row2Cell().csvEscape(forceEscape = forceEscape)
    private suspend fun FlowCollector<String>.emitRow(transform: (CsvColumn<Row>) -> String) {
        schema.columns.forEachIndexed { index, column ->
            if (index != 0) emit(config.delimiter.toString())
            emit(transform(column))
        }
    }

    private fun String.csvEscape(forceEscape: Boolean) =
        if ((forceEscape && isNotEmpty()) || needsEscaping())
            "${CsvConfig.CSV_QUOTE}${
                replace(
                    CsvConfig.CSV_QUOTE_STR,
                    CsvConfig.CSV_ESCAPED_QUOTE_STR
                )
            }${CsvConfig.CSV_QUOTE}"
        else
            this

    /*
    private fun String.csvUnescape() =
        if (length > 1 && first() == CSV_QUOTE && last() == CSV_QUOTE)
            substring(1, length - 1).replace(CSV_ESCAPED_QUOTE_STR, CSV_QUOTE_STR)
        else
            this
    */

    private fun String.needsEscaping(): Boolean {
        val escapeChars = charArrayOf(config.delimiter, CsvConfig.CSV_QUOTE, CsvConfig.CSV_NEW_LINE, '\r')
        return any { it in escapeChars }
    }

    public class Builder<Row>(
        private var config: CsvConfig? = null,
        private var schema: CsvSchema<Row>? = null,
    ) {
        public fun config(block: CsvConfig.Builder.() -> Unit) {
            config = CsvConfig.Builder().apply(block).build()
        }

        public fun schema(block: CsvSchema.Builder<Row>.() -> Unit) {
            schema = CsvSchema.Builder<Row>().apply(block).build()
        }

        public fun setSchema(schema: CsvSchema<Row>) {
            this.schema = schema
        }

        public fun setConfig(config: CsvConfig) {
            this.config = config
        }

        internal fun build() = CsvSerializer(
            config = config ?: CsvConfig.Builder().build(),
            schema = schema ?: CsvSchema.Builder<Row>().build(),
        )
    }
}