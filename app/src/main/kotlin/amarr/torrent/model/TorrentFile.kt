package amarr.torrent.model

import kotlinx.serialization.Serializable

@Serializable
data class TorrentFile(
    val name: String,
)
