package amarr

import com.google.common.io.BaseEncoding.base32
import io.ktor.http.*

class MagnetLink(
    val hash: ByteArray,
    val name: String,
    val size: Long,
    val trackers: List<String>,
) {
    @OptIn(ExperimentalStdlibApi::class)
    fun toEd2kLink(): String {
        // unpad the hash to ensure a size of 128 bits, then encode it as hex
        val hexHash = hash.copyOf(16).toHexString()
        return "ed2k://|file|${name.encodeURLParameter()}|$size|$hexHash|/"
    }

    fun isAmarr(): Boolean {
        return trackers.contains(AMARR_TRACKER)
    }

    override fun toString(): String {
        // pad the hash to ensure a size of 160 bits
        val hash = hash.copyOf(20)
        val base32Hash = base32().encode(hash)
        return "magnet:" +
                "?xt=urn:btih:$base32Hash" +
                "&dn=${name.encodeURLParameter()}" +
                "&xl=$size" +
                "&tr=${trackers.joinToString("&tr=") { it.encodeURLParameter() }}"
    }

    companion object {
        fun forAmarr(hash: ByteArray, name: String, size: Long) = MagnetLink(
            hash = hash,
            name = name,
            size = size,
            trackers = listOf(AMARR_TRACKER)
        )

        fun fromString(magnet: String): MagnetLink = magnet
            .substringAfter("magnet:?")
            .split("&")
            .filter { it.matches(Regex(".+=.+")) }
            .map { val els = it.split("="); els[0] to els[1] }
            .let { params ->
                val hash = base32().decode(params.first { it.first == "xt" }.second.substringAfter("urn:btih:"))
                MagnetLink(
                    hash = hash,
                    name = params.first { it.first == "dn" }.second.decodeURLPart(),
                    size = params.first { it.first == "xl" }.second.toLong(),
                    trackers = params.filter { it.first == "tr" }.map { it.second.decodeURLPart() }
                )
            }

        const val AMARR_TRACKER = "http://amarr-reserved"
    }
}