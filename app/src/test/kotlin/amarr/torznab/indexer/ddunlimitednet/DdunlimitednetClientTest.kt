package amarr.torznab.indexer.ddunlimitednet

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.utils.io.*
import org.slf4j.LoggerFactory

class DdunlimitednetClientTest : StringSpec({
    val logger = LoggerFactory.getLogger(this::class.java)

    "should parse standard search" {
        val html = this::class.java.getResource("/ddunlimitednet/search-matrix.html")!!.readText()

        val mockEngine = MockEngine { _ -> respond(ByteReadChannel(html), OK) }
        val client = DdunlimitednetClient(mockEngine, "user", "pass", logger)

        val result = client.search("matrix", listOf())

        result.isSuccess shouldBe true
        result.getOrThrow().size shouldBe 110
    }

    "should decode html and remove tags" {
        val html =
            "ed2k://|file|Dj%20<tag>Matrix</tag>%20&amp;%20Matt%20Joe%20-%20Musica%20da%20giostra,%20Vol.%2010%20&#40;2023&#41;.rar|152488462|0320C47B3BAA01F8D5F42CD7C05CE28D|h=O74TQQWUVF24E7WD25UD57Z45GHIDLZZ|/"

        val mockEngine = MockEngine { _ -> respond(ByteReadChannel(html), OK) }
        val client = DdunlimitednetClient(mockEngine, "user", "pass", logger)

        val result = client.search("matrix", listOf())

        result.isSuccess shouldBe true
        result.getOrThrow().size shouldBe 1
        result.getOrThrow()[0] shouldBe "ed2k://|file|Dj%20Matrix%20&%20Matt%20Joe%20-%20Musica%20da%20giostra,%20Vol.%2010%20(2023).rar|152488462|0320C47B3BAA01F8D5F42CD7C05CE28D|h=O74TQQWUVF24E7WD25UD57Z45GHIDLZZ|/"
    }

})
