package amarr.torrent.model

import kotlinx.serialization.Serializable

/**
 * Full documentation of the qBittorrent API can be found here:
 * https://github.com/qbittorrent/qBittorrent/wiki/WebUI-API-(qBittorrent-4.1)
 */
@Serializable
data class TorrentInfo(
    // Following values are used by Radarr
    val hash: String, // Torrent hash
    val name: String, // Torrent name
    val size: Long, // Total size (bytes) of files selected for download
    val progress: Double, // Torrent progress (percentage/100)
    val eta: Int, // Torrent ETA (seconds) the value 8640000 indicates that there is no ETA available
    val state: TorrentState, // Torrent state
    val category: String?, // Category of the torrent
    val save_path: String, // Path where this torrent's data is stored

    // Following are not used by Radarr but are handled by amarr
    val dlspeed: Long, // Torrent download speed (bytes/s)
    val num_seeds: Int, // Number of seeders connected to this torrent
    val priority: Int, // Torrent priority. Returns -1 if queuing is disabled or torrent is in seed mode
    val total_size: Long, // Total size (bytes) of all file in torrent
    val downloaded: Long, // Amount of data (bytes) downloaded since torrent was started

    // Following are parsed by Radarr but not handled by amarr yet
    // TODO: Handle these values
    val content_path: String = "", // Subpath where this torrent's data is stored. Only available for multifile torrents
    val ratio: Double = 0.0, // Torrent share ratio. Max ratio value: 9999.
    val ratio_limit: Int = -2, // Max share ratio until torrent is stopped from seeding/uploading -2 = Use global share ratio limit -1 = Unlimited
    val seeding_time: Int = 0, // Total time (seconds) this torrent has been seeding
    val seeding_time_limit: Int = -2, // Max seeding time (seconds) until torrent is stopped from seeding -2 = Use global seeding time limit -1 = Unlimited

    // TODO This is not parsed by Radarr but should be handled by amarr
    val magnet_uri: String = "magnet:?xt=urn:btih:58d3afd393bb1748dc25e24fc680f032a475fa63&dn=Matrix%20HQ%20movie%201998&tr=udp%3a%2f%2ftracker.opentrackr.org%3a1337%2fannounce&tr=https%3a%2f%2ftracker2.ctix.cn%3a443%2fannounce&tr=https%3a%2f%2ftracker1.520.jp%3a443%2fannounce&tr=udp%3a%2f%2fopentracker.i2p.rocks%3a6969%2fannounce&tr=udp%3a%2f%2fopen.tracker.cl%3a1337%2fannounce&tr=udp%3a%2f%2fopen.demonii.com%3a1337%2fannounce&tr=udp%3a%2f%2ftracker.openbittorrent.com%3a6969%2fannounce&tr=http%3a%2f%2ftracker.openbittorrent.com%3a80%2fannounce&tr=udp%3a%2f%2fopen.stealth.si%3a80%2fannounce&tr=udp%3a%2f%2fexodus.desync.com%3a6969%2fannounce&tr=udp%3a%2f%2ftracker.torrent.eu.org%3a451%2fannounce&tr=udp%3a%2f%2ftracker1.bt.moack.co.kr%3a80%2fannounce&tr=udp%3a%2f%2ftracker-udp.gbitt.info%3a80%2fannounce&tr=udp%3a%2f%2fexplodie.org%3a6969%2fannounce&tr=https%3a%2f%2ftracker.gbitt.info%3a443%2fannounce&tr=http%3a%2f%2ftracker.gbitt.info%3a80%2fannounce&tr=http%3a%2f%2fbt.endpot.com%3a80%2fannounce&tr=udp%3a%2f%2ftracker.tiny-vps.com%3a6969%2fannounce&tr=udp%3a%2f%2ftracker.auctor.tv%3a6969%2fannounce&tr=udp%3a%2f%2ftk1.trackerservers.com%3a8080%2fannounce",

    // Following values are not handled by amarr
    val upspeed: Long = 0, // Torrent upload speed (bytes/s)
    val num_leechs: Int = 0, // Number of leechers connected to this torrent
    val tags: String = "", // Comma-concatenated tag list of the torrent
    val super_seeding: Boolean = false, // True if super seeding is enabled
    val added_on: Long = 1696781958, // TODO: Change (UTC timestamp)
    val amount_left: Int = 0,
    val auto_tmm: Boolean = false,
    val availability: Int = 0,
    val completed: Int = 0,
    val completion_on: Int = 0,
    val dl_limit: Int = 0,
    val download_path: String = "",
    val downloaded_session: Int = 0,
    val f_l_piece_prio: Boolean = false,
    val force_start: Boolean = false,
    val last_activity: Long = 1696781958, // TODO: Change (UTC timestamp)
    val max_ratio: Int = -1,
    val max_seeding_time: Int = -1,
    val num_complete: Int = -1,
    val num_incomplete: Int = -1,
    val seen_complete: Int = 0,
    val seq_dl: Boolean = false,
    val time_active: Int = 309,
    val tracker: String = "http://tracker.openbittorrent.com:80/announce",
    val trackers_count: Int = 20,
    val up_limit: Int = 0,
    val uploaded: Int = 0,
    val uploaded_session: Int = 0,
)

enum class TorrentState {
    // Maps to an error in Radarr (Status "Warning")
    error, // Some error occurred, applies to paused torrents
    stalledDL, // Torrent is being downloaded, but no connection were made
    missingFiles, // Torrent data files is missing

    // Maps to the "Paused" state in Radarr
    pausedDL, // Torrent is paused and has NOT finished downloading

    // All map to the "Queued" state in Radarr
    queuedDL, // Queuing is enabled and torrent is queued for download
    checkingDL, // Same as checkingUP, but torrent has NOT finished downloading
    checkingUP, // Torrent has finished downloading and is being checked
    checkingResumeData, // Checking resume data on qBt startup

    // All map to the "Completed" state in Radarr
    pausedUP, // Torrent is paused and has finished downloading
    uploading, // Torrent is being seeded and data is being transferred
    stalledUP, // Torrent is being seeded, but no connection were made
    queuedUP, // Queuing is enabled and torrent is queued for upload
    forcedUP, // Torrent is forced to uploading and ignore queue limit

    // Maps to the "Queued" state in Radarr only if Dht is enabled, else "Warning"
    metaDL, // Torrent has just started downloading and is fetching metadata

    // Maps to the "Downloading" state in Radarr
    forcedDL, // Torrent is forced to downloading to ignore queue limit
    moving, // Torrent is moving to another location
    downloading, // Torrent is being downloaded and data is being transferred

    // Maps to the "Unknown" state in Radarr
    allocating, // Torrent is allocating disk space for download
    unknown, // Unknown status
}