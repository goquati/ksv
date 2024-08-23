package io.github.goquati.ksv

import kotlin.reflect.KProperty1


public data class CsvSchema<Row>(
    val columns: List<CsvColumn<Row>>,
) {

    public class Builder<Row> {
        private val columns: MutableList<CsvColumn<Row>> = mutableListOf()

        public fun column(
            name: String,
            forceEscape: Boolean = false,
            row2Cell: Row.() -> String,
        ): Boolean = columns.add(
            CsvColumn(
                name = name,
                forceEscape = forceEscape,
                row2Cell = row2Cell,
            )
        )

        public fun column(
            field: KProperty1<Row, *>,
            name: String? = null,
            forceEscape: Boolean = false,
        ): Boolean = columns.add(
            CsvColumn(
                name = name ?: field.name,
                forceEscape = forceEscape,
                row2Cell = {
                    val value = field.get(this)
                        ?: return@CsvColumn ""
                    value.toString()
                }
            )
        )

        internal fun build() = CsvSchema(columns = columns.toList())
    }
}
