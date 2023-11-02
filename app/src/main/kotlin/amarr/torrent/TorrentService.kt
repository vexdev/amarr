package amarr.torrent

import amarr.MagnetLink
import amarr.category.CategoryStore
import amarr.torrent.model.*
import io.ktor.server.plugins.*
import io.ktor.util.logging.*
import jamule.AmuleClient
import jamule.model.AmuleTransferringFile
import jamule.model.DownloadCommand
import jamule.model.FileStatus

class TorrentService(
    private val amuleClient: AmuleClient,
    private val categoryStore: CategoryStore,
    private val finishedPath: String,
    private val log: Logger
) {

    fun getTorrentInfo(category: String?): List<TorrentInfo> {
        val downloadingFiles = amuleClient
            .getDownloadQueue()
            .getOrThrow()
        val sharedFiles = amuleClient.getSharedFiles().getOrThrow()
        val downloadingFilesHashSet = downloadingFiles.map { it.fileHashHexString }.toHashSet()

        val allFiles = (sharedFiles // Downloading files also appear in shared files
            .filterNot { downloadingFilesHashSet.contains(it.fileHashHexString) } + downloadingFiles)
            .filter { category == null || categoryStore.getCategory(it.fileHashHexString!!) == category }

        return allFiles
            .map { dl ->
                if (dl is AmuleTransferringFile)
                    TorrentInfo(
                        hash = dl.fileHashHexString!!,
                        name = dl.fileName!!,
                        size = dl.sizeFull!!,
                        total_size = dl.sizeFull!!,
                        save_path = finishedPath,
                        downloaded = dl.sizeDone!!,
                        progress = dl.sizeDone!!.toDouble() / dl.sizeFull!!.toDouble(),
                        priority = dl.downPrio.toInt(),
                        state = if (dl.sourceXferCount > 0) TorrentState.downloading
                        else when (dl.fileStatus) {
                            FileStatus.READY -> TorrentState.metaDL
                            FileStatus.ERROR -> TorrentState.error
                            FileStatus.COMPLETING -> TorrentState.checkingDL
                            FileStatus.COMPLETE -> TorrentState.uploading
                            FileStatus.PAUSED -> TorrentState.pausedDL
                            FileStatus.ALLOCATING -> TorrentState.allocating
                            FileStatus.INSUFFICIENT -> TorrentState.error
                                .also { log.error("Insufficient disk space") }

                            else -> TorrentState.unknown
                        },
                        category = category,
                        dlspeed = dl.speed!!,
                        num_seeds = dl.sourceXferCount.toInt(),
                        eta = computeEta(dl.speed!!, dl.sizeFull!!, dl.sizeDone!!),
                    )
                else
                // File is already fully downloaded
                    TorrentInfo(
                        hash = dl.fileHashHexString!!,
                        name = dl.fileName!!,
                        size = dl.sizeFull!!,
                        total_size = dl.sizeFull!!,
                        save_path = finishedPath,
                        dlspeed = 0,
                        downloaded = dl.sizeFull!!,
                        progress = 1.0,
                        priority = 0,
                        state = TorrentState.uploading,
                        category = category,
                        eta = 0,
                        num_seeds = 0, // Irrelevant
                    )
            }
    }

    private fun computeEta(speed: Long, sizeFull: Long, sizeDone: Long): Int {
        val remainingBytes = sizeFull - sizeDone
        return if (speed == 0L) 8640000 else Math.min((remainingBytes / speed).toInt(), 8640000)
    }

    fun getCategories(): Map<String, Category> = categoryStore
        .getCategories()
        .associateBy { it.name }

    fun addCategory(category: Category) = categoryStore.addCategory(category)

    fun addTorrent(urls: List<String>?, category: String?, paused: String?) {
        if (urls == null) {
            log.error("No urls provided")
            throw nonAmarrLink("No urls provided")
        }
        urls.forEach { url ->
            val magnetLink = try {
                MagnetLink.fromString(url)
            } catch (e: Exception) {
                throw nonAmarrLink(url)
            }
            if (!magnetLink.isAmarr()) {
                throw nonAmarrLink(url)
            }
            amuleClient.downloadEd2kLink(magnetLink.toEd2kLink())
            if (category != null) {
                categoryStore.store(category, magnetLink.amuleHexHash())
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun deleteTorrent(hashes: List<String>, deleteFiles: String?) = hashes.forEach { hash ->
        amuleClient.sendDownloadCommand(hash.hexToByteArray(), DownloadCommand.DELETE)
        categoryStore.delete(hash)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun deleteAllTorrents(deleteFiles: String?) = amuleClient.getSharedFiles().getOrThrow().forEach { file ->
        amuleClient.sendDownloadCommand(file.fileHashHexString!!.hexToByteArray(), DownloadCommand.DELETE)
        categoryStore.delete(file.fileHashHexString!!)
    }

    fun getFile(hash: String) = getTorrentInfo(null)
        .first { it.hash == hash }
        .let {
            TorrentFile(
                name = it.name,
            )
        }

    fun getTorrentProperties(hash: String): TorrentProperties = getTorrentInfo(null)
        .first { it.hash == hash }
        .let {
            TorrentProperties(
                hash = it.hash,
                save_path = it.save_path,
                seeding_time = 0,
            )
        }

    private fun nonAmarrLink(url: String): Exception {
        log.error(
            "The provided link does not appear to be an Amarr link: {}. " +
                    "Have you configured Radarr/Sonarr's download client priority correctly? See README.md", url
        )
        return NotFoundException("The provided link does not appear to be an Amarr link: $url")
    }

}