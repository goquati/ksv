package io.github.goquati.ksv

import kotlinx.coroutines.flow.Flow
import kotlinx.io.bytestring.ByteString

public fun <Row> csvSchema(block: CsvSchema.Builder<Row>.() -> Unit): CsvSchema<Row> =
    CsvSchema.Builder<Row>().apply(block).build()

public fun <Row> csvSerializer(block: CsvSerializer.Builder<Row>.() -> Unit): CsvSerializer<Row> =
    CsvSerializer.Builder<Row>().apply(block).build()

public fun <Row> Flow<Row>.streamCsv(serializer: CsvSerializer<Row>): Flow<ByteString> =
    serializer.streamCsv(data = this)

public fun <Row> Flow<Row>.streamCsv(
    block: CsvSerializer.Builder<Row>.() -> Unit,
): Flow<ByteString> = CsvSerializer.Builder<Row>().apply(block).build().streamCsv(data = this)

public fun <Row> Flow<Row>.serializeCsv(serializer: CsvSerializer<Row>): Flow<String> =
    serializer.serializeCsv(data = this)

public fun <Row> Flow<Row>.serializeCsv(
    block: CsvSerializer.Builder<Row>.() -> Unit,
): Flow<String> = CsvSerializer.Builder<Row>().apply(block).build().serializeCsv(data = this)
