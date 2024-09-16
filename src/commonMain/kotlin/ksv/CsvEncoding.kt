package io.github.goquati.ksv

public sealed interface CsvEncoding {
    public val bom: String

    public data object UTF_8 : CsvEncoding {
        public override val bom: String = "\uFEFF"
    }
}
