package amarr

import com.google.common.io.BaseEncoding.base32
import io.ktor.http.*

data class MagnetLink(
    private val hash: ByteArray,
    val name: String,
    val size: Long,
    val trackers: List<String>,
) {
    fun toEd2kLink(): String {
        return "ed2k://|file|${name.encodeURLParameter()}|$size|${amuleHexHash()}|/"
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun amuleHexHash(): String {
        // unpad the hash to ensure a size of 128 bits, then encode it as hex
        return hash.copyOf(16).toHexString()
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MagnetLink

        if (!hash.contentEquals(other.hash)) return false
        if (name != other.name) return false
        if (size != other.size) return false
        if (trackers != other.trackers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hash.contentHashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + trackers.hashCode()
        return result
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

        @OptIn(ExperimentalStdlibApi::class)
        fun fromEd2k(ed2k: String): MagnetLink = ed2k
            .substringAfter("ed2k://|file|")
            .substringBefore("|/")
            .split("|")
            .let { els ->
                MagnetLink(
                    hash = els[2].hexToByteArray(),
                    name = els[0].decodeURLPart(),
                    size = els[1].toLong(),
                    trackers = listOf(AMARR_TRACKER)
                )
            }

        const val AMARR_TRACKER = "http://amarr-reserved"
    }
}