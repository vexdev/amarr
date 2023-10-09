package amarr.amule

import com.google.common.io.BaseEncoding
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

    override fun toString(): String {
        // pad the hash to ensure a size of 160 bits
        val hash = hash.copyOf(20)
        val base32Hash = BaseEncoding.base32().encode(hash)
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
            trackers = listOf("http://amarr-reserved")
        )
    }
}