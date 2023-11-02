package amarr

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.ktor.http.*

class MagnetLinkTest : StringSpec({

    val magnetArb = arbitrary {
        val hash = Arb.byteArray(Arb.int(20, 20), Arb.byte()).bind()
        val name = Arb.string(1..100).bind()
        val size = Arb.long(0..Long.MAX_VALUE).bind()
        MagnetLink.forAmarr(hash, name, size)
    }

    "should create and parse magnet links" {
        checkAll(magnetArb) { magnet ->
            val parsed = MagnetLink.fromString(magnet.toString())
            parsed shouldBe magnet
            parsed.isAmarr() shouldBe true
            parsed.amuleHexHash().length shouldBe 32
            parsed.toEd2kLink() shouldBe "ed2k://|file|${magnet.name.encodeURLParameter()}|${magnet.size}|${magnet.amuleHexHash()}|/"
        }
    }

    "should parse sample ed2k" {
        val ed2k = "ed2k://|file|Dj%20Matrix%20&%20Matt%20Joe%20-%20Musica%20da%20giostra,%20Vol.%2010%20(2023).rar|152488462|0320C47B3BAA01F8D5F42CD7C05CE28D|h=O74TQQWUVF24E7WD25UD57Z45GHIDLZZ|/"
        val parsed = MagnetLink.fromEd2k(ed2k)
        parsed.isAmarr() shouldBe true
        parsed.name shouldBe "Dj Matrix & Matt Joe - Musica da giostra, Vol. 10 (2023).rar"
        parsed.size shouldBe 152488462
        parsed.amuleHexHash().uppercase() shouldBe "0320C47B3BAA01F8D5F42CD7C05CE28D"
    }
})