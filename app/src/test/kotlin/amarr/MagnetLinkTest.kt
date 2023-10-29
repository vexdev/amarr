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
})