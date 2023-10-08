package amarr.amule.model

import java.util.*

data class Download(
    val hash: String,
    val fileName: String,
    val ed2kLink: String,
    val status: DownloadStatus,
    val prio: DownloadPriority,
    val cat: Long,
    val sourceCount: Int,
    val metID: Int,
    val sourceA4AF: Int,
    val sourceXfer: Int,
    val sourceNotCurrent: Int,
    val sizeXfer: Long,
    val sizeFull: Long,
    val sizeDone: Long,
    val speed: Long,
    val lastSeenComp: Date,
    val lastRecv: Date,
)

enum class DownloadStatus(val value: Byte) {
    PS_READY(0x0),
    PS_EMPTY(0x1),
    PS_WAITINGFORHASH(0x2),
    PS_HASHING(0x3),
    PS_ERROR(0x4),
    PS_INSUFFICIENT(0x5),
    PS_UNKNOWN(0x6),
    PS_PAUSED(0x7),
    PS_COMPLETING(0x8),
    PS_COMPLETE(0x9),
    PS_ALLOCATING(0x10);

    companion object {
        fun fromValue(value: Byte) = entries.first { it.value == value }
    }
}

enum class DownloadPriority(val value: Byte) {
    PR_LOW(0x0),
    PR_NORMAL(0x1),
    PR_HIGH(0x2),
    PR_AUTO(0x5),
    PR_AUTO_LOW(0xA),
    PR_AUTO_NORMAL(0xB),
    PR_AUTO_HIGH(0xC);

    companion object {
        fun fromValue(value: Byte) = entries.first { it.value == value }
    }
}