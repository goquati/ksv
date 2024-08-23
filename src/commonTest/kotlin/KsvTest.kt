import io.github.goquati.ksv.*
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToString
import kotlin.test.Test

class KsvTest {
    companion object {
        private suspend fun Flow<String>.concat() = toList().joinToString("")
        private suspend fun Flow<ByteString>.concatToString() = map { it.decodeToString() }.toList().joinToString("")

        private suspend fun <Row> test(
            input: Flow<Row>,
            serializer: CsvSerializer.Builder<Row>.() -> Unit,
            expectedOutput: String,
        ) {
            csvSerializer(block = serializer).streamCsv(input).concatToString() shouldBe expectedOutput
            input.streamCsv(block = serializer).concatToString() shouldBe expectedOutput
            input.streamCsv(serializer = csvSerializer(block = serializer)).concatToString() shouldBe expectedOutput
            csvSerializer(block = serializer).serializeCsv(input).concat() shouldBe expectedOutput
            input.serializeCsv(block = serializer).concat() shouldBe expectedOutput
            input.serializeCsv(serializer = csvSerializer(block = serializer)).concat() shouldBe expectedOutput
        }
    }

    private data class RowData(
        val title: String,
        val amount: Double,
        val description: String?,
    )

    private data class SimpleRowData(
        val text: String,
    )

    @Test
    fun `test basic`(): Unit = runBlocking {
        val input = flowOf(
            RowData(title = "World", amount = 3.0, description = null),
            RowData(title = "Hello", amount = 47.0, description = "hello world"),
        )
        test(
            input = input,
            serializer = {
                schema {
                    column(RowData::title)
                    column(RowData::amount)
                    column(RowData::description)
                }
            },
            expectedOutput = """
                title,amount,description
                World,3.0,
                Hello,47.0,hello world${'\n'}
            """.trimIndent(),
        )
        test(
            input = input,
            serializer = {
                setConfig(
                    CsvConfig(
                        withBom = false,
                        withHeader = true,
                        delimiter = ',',
                        encoding = Encoding.UTF_8,
                    )
                )
                setSchema(csvSchema {
                    column(name = "my-title") { title }
                    column(name = "my-amount") { amount.toString() }
                    column(name = "my-description") { description ?: "" }
                })
            },
            expectedOutput = """
                my-title,my-amount,my-description
                World,3.0,
                Hello,47.0,hello world${'\n'}
            """.trimIndent(),
        )
    }


    @Test
    fun `test BOM`(): Unit = runBlocking {
        val input = flowOf(
            RowData(title = "World", amount = 3.0, description = null),
            RowData(title = "Hello", amount = 47.0, description = "hello world"),
        )

        fun getSerializerBuilder(bom: Boolean): CsvSerializer.Builder<RowData>.() -> Unit = {
            config {
                withBom = bom
            }
            schema {
                column(RowData::title)
                column(RowData::amount)
                column(RowData::description)
            }
        }

        val expectedResult = """
            title,amount,description
            World,3.0,
            Hello,47.0,hello world${'\n'}
        """.trimIndent()

        test(input = input, serializer = getSerializerBuilder(bom = false), expectedOutput = expectedResult)
        test(input = input, serializer = getSerializerBuilder(bom = true), expectedOutput = "\uFEFF$expectedResult")
    }


    @Test
    fun `test header`(): Unit = runBlocking {
        val input = flowOf(
            RowData(title = "World", amount = 3.0, description = null),
            RowData(title = "Hello", amount = 47.0, description = "hello world"),
        )

        fun getSerializerBuilder(header: Boolean): CsvSerializer.Builder<RowData>.() -> Unit = {
            config {
                withHeader = header
            }
            schema {
                column(RowData::title)
                column(RowData::amount)
                column(RowData::description)
            }
        }
        test(
            input = input,
            serializer = getSerializerBuilder(header = false),
            expectedOutput = """
                World,3.0,
                Hello,47.0,hello world${'\n'}
            """.trimIndent()
        )
        test(
            input = input,
            serializer = getSerializerBuilder(header = true),
            expectedOutput = """
                title,amount,description
                World,3.0,
                Hello,47.0,hello world${'\n'}
            """.trimIndent()
        )
    }


    @Test
    fun `test empty`(): Unit = runBlocking {
        test(
            input = flowOf<RowData>(),
            serializer = {},
            expectedOutput = "\n",
        )
        test(
            input = flowOf(
                RowData(title = "World", amount = 3.0, description = null),
                RowData(title = "Hello", amount = 47.0, description = "hello world"),
            ),
            serializer = {},
            expectedOutput = "\n\n\n",
        )
    }

    @Test
    fun `test escape`(): Unit = runBlocking {
        test(
            input = flowOf<RowData>(),
            serializer = {},
            expectedOutput = "\n",
        )
        test(
            input = flowOf(
                RowData(title = "World", amount = 3.0, description = null),
                RowData(title = "Hello", amount = 47.0, description = "hello world"),
            ),
            serializer = {},
            expectedOutput = "\n\n\n",
        )
        test(
            input = flowOf(
                SimpleRowData("hel\"lo"),
            ),
            serializer = {
                schema { column(SimpleRowData::text, name = "te\"xt") }
            },
            expectedOutput = "\"te\"\"xt\"\n\"hel\"\"lo\"\n",
        )
        test(
            input = flowOf(
                SimpleRowData("hel\nlo"),
            ),
            serializer = {
                schema { column(SimpleRowData::text, name = "te\nxt") }
            },
            expectedOutput = "\"te\nxt\"\n\"hel\nlo\"\n",
        )
        test(
            input = flowOf(
                SimpleRowData("hel\rlo"),
            ),
            serializer = {
                schema { column(SimpleRowData::text, name = "te\rxt") }
            },
            expectedOutput = "\"te\rxt\"\n\"hel\rlo\"\n",
        )
        test(
            input = flowOf(
                SimpleRowData("hel,lo"),
            ),
            serializer = {
                schema { column(SimpleRowData::text, name = "te,xt") }
            },
            expectedOutput = "\"te,xt\"\n\"hel,lo\"\n",
        )
        test(
            input = flowOf(
                SimpleRowData("hel,lo"),
            ),
            serializer = {
                config { delimiter = ';' }
                schema { column(SimpleRowData::text, name = "te,xt") }
            },
            expectedOutput = "te,xt\nhel,lo\n",
        )
        test(
            input = flowOf(
                SimpleRowData("hel;lo"),
            ),
            serializer = {
                config { delimiter = ';' }
                schema { column(SimpleRowData::text, name = "te;xt") }
            },
            expectedOutput = "\"te;xt\"\n\"hel;lo\"\n",
        )
    }

    @Test
    fun `test force escape`(): Unit = runBlocking {
        test(
            input = flowOf(
                SimpleRowData("hello"),
                SimpleRowData(""),
                SimpleRowData("hel,lo"),
            ),
            serializer = {
                schema { column(SimpleRowData::text, forceEscape = true) }
            },
            expectedOutput = "text\n\"hello\"\n\n\"hel,lo\"\n",
        )
        test(
            input = flowOf(
                SimpleRowData("hello"),
                SimpleRowData(""),
                SimpleRowData("\""),
                SimpleRowData("hel,lo"),
            ),
            serializer = {
                schema { column(name = "text", forceEscape = true) { text } }
            },
            expectedOutput = "text\n\"hello\"\n\n\"\"\"\"\n\"hel,lo\"\n",
        )
    }
}