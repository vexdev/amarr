package amarr.torrent.model

import kotlinx.serialization.Serializable

@Serializable
data class TorrentInfo(
    val added_on: Long = 1696781958, // TODO: Change (UTC timestamp)
    val amount_left: Int = 0,
    val auto_tmm: Boolean = false,
    val availability: Int = 0,
    val category: String?,
    val completed: Int = 0,
    val completion_on: Int = 0,
    val content_path: String = "",
    val dl_limit: Int = 0,
    val dlspeed: Int = 0,
    val download_path: String = "",
    val downloaded: Int = 0,
    val downloaded_session: Int = 0,
    val eta: Int = 8640000,
    val f_l_piece_prio: Boolean = false,
    val force_start: Boolean = false,
    val hash: String = "58d3afd393bb1748dc25e24fc680f032a475fa63",
    val infohash_v1: String = "58d3afd393bb1748dc25e24fc680f032a475fa63",
    val infohash_v2: String = "",
    val last_activity: Long = 1696781958, // TODO: Change (UTC timestamp)
    val magnet_uri: String = "magnet:?xt=urn:btih:58d3afd393bb1748dc25e24fc680f032a475fa63&dn=Matrix%20HQ%20movie%201998&tr=udp%3a%2f%2ftracker.opentrackr.org%3a1337%2fannounce&tr=https%3a%2f%2ftracker2.ctix.cn%3a443%2fannounce&tr=https%3a%2f%2ftracker1.520.jp%3a443%2fannounce&tr=udp%3a%2f%2fopentracker.i2p.rocks%3a6969%2fannounce&tr=udp%3a%2f%2fopen.tracker.cl%3a1337%2fannounce&tr=udp%3a%2f%2fopen.demonii.com%3a1337%2fannounce&tr=udp%3a%2f%2ftracker.openbittorrent.com%3a6969%2fannounce&tr=http%3a%2f%2ftracker.openbittorrent.com%3a80%2fannounce&tr=udp%3a%2f%2fopen.stealth.si%3a80%2fannounce&tr=udp%3a%2f%2fexodus.desync.com%3a6969%2fannounce&tr=udp%3a%2f%2ftracker.torrent.eu.org%3a451%2fannounce&tr=udp%3a%2f%2ftracker1.bt.moack.co.kr%3a80%2fannounce&tr=udp%3a%2f%2ftracker-udp.gbitt.info%3a80%2fannounce&tr=udp%3a%2f%2fexplodie.org%3a6969%2fannounce&tr=https%3a%2f%2ftracker.gbitt.info%3a443%2fannounce&tr=http%3a%2f%2ftracker.gbitt.info%3a80%2fannounce&tr=http%3a%2f%2fbt.endpot.com%3a80%2fannounce&tr=udp%3a%2f%2ftracker.tiny-vps.com%3a6969%2fannounce&tr=udp%3a%2f%2ftracker.auctor.tv%3a6969%2fannounce&tr=udp%3a%2f%2ftk1.trackerservers.com%3a8080%2fannounce", // TODO: Change
    val max_ratio: Int = -1,
    val max_seeding_time: Int = -1,
    val name: String = "Matrix HQ movie 1998", // TODO: Change
    val num_complete: Int = 0,
    val num_incomplete: Int = 1,
    val num_leechs: Int = 0,
    val num_seeds: Int = 0,
    val priority: Int = 1,
    val progress: Int = 0,
    val ratio: Int = 0,
    val ratio_limit: Int = -2,
    val save_path: String = "/media/usb-luca/Downloads", // TODO: Change
    val seeding_time: Int = 0,
    val seeding_time_limit: Int = -2,
    val seen_complete: Int = 0,
    val seq_dl: Boolean = false,
    val size: Int = 0,
    val state: String = "metaDL",
    val super_seeding: Boolean = false,
    val tags: String = "",
    val time_active: Int = 309,
    val total_size: Int = -1,
    val tracker: String = "http://tracker.openbittorrent.com:80/announce",
    val trackers_count: Int = 20,
    val up_limit: Int = 0,
    val uploaded: Int = 0,
    val uploaded_session: Int = 0,
    val upspeed: Int = 0
)
