package io.github.goquati.ksv

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.buildByteString


public sealed interface Encoding {
    public val bom: ByteString

    public data object UTF_8 : Encoding {
        public override val bom: ByteString = buildByteString {
            append(0xEF.toByte())
            append(0xBB.toByte())
            append(0xBF.toByte())
        }
    }
}
