package amarr.torrent.model

import kotlinx.serialization.Serializable

@Serializable
data class TorrentProperties(
    val hash: String,
    val save_path: String,
    val seeding_time: Long,
)
