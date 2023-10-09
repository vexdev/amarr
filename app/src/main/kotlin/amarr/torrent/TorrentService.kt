package amarr.torrent

import amarr.FINISHED_FOLDER
import amarr.amule.AmuleClient
import amarr.amule.model.DownloadPriority
import amarr.amule.model.DownloadStatus
import amarr.torrent.model.Category
import amarr.torrent.model.TorrentInfo
import amarr.torrent.model.TorrentState
import io.ktor.util.logging.*

class TorrentService(private val amuleClient: AmuleClient, private val log: Logger) {
    private val categoryList = mutableListOf<Category>()

    fun getTorrentInfo(category: String?) = amuleClient
        .listDownloads()
        .map { dl ->
            TorrentInfo(
                hash = dl.hash,
                name = dl.fileName,
                size = dl.sizeFull,
                total_size = dl.sizeFull,
                downloaded = dl.sizeDone,
                progress = dl.sizeDone.toDouble() / dl.sizeFull.toDouble(),
                priority = when (dl.prio) {
                    DownloadPriority.PR_LOW -> 1
                    DownloadPriority.PR_NORMAL -> 2
                    DownloadPriority.PR_HIGH -> 3
                    else -> -1
                },
                state = if (dl.sourceXfer > 0) TorrentState.downloading
                else when (dl.status) {
                    DownloadStatus.PS_READY -> TorrentState.metaDL
                    DownloadStatus.PS_ERROR -> TorrentState.error
                    DownloadStatus.PS_COMPLETING -> TorrentState.checkingDL
                    DownloadStatus.PS_COMPLETE -> TorrentState.uploading
                    DownloadStatus.PS_PAUSED -> TorrentState.pausedDL
                    DownloadStatus.PS_ALLOCATING -> TorrentState.allocating
                    DownloadStatus.PS_INSUFFICIENT -> TorrentState.error
                        .also { log.error("Insufficient disk space") }

                    else -> TorrentState.unknown
                },
                category = category ?: categoryList.firstOrNull()?.name,
                save_path = FINISHED_FOLDER,
                dlspeed = dl.speed,
                num_seeds = dl.sourceCount,
                eta = computeEta(dl.speed, dl.sizeFull, dl.sizeDone),
            )
        }

    private fun computeEta(speed: Long, sizeFull: Long, sizeDone: Long): Int {
        val remainingBytes = sizeFull - sizeDone
        return if (speed == 0L) 8640000 else Math.min((remainingBytes / speed).toInt(), 8640000)
    }

    fun getCategories(): Map<String, Category> = categoryList.associateBy { it.name }

    fun addCategory(category: String) {
        categoryList.add(Category(category, ""))
    }

    fun addTorrent(urls: List<String>, category: String?, paused: String?) {
        urls.forEach { magnetLink ->
            amuleClient.download(magnetLink)
        }
    }

    fun deleteTorrent(hashes: List<String>, deleteFiles: String?) {
        amuleClient.delete(hashes)
    }

    fun deleteAllTorrents(deleteFiles: String?) {
        TODO("Not yet implemented")
    }

}