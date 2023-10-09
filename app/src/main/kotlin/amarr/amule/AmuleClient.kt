package amarr.amule

import amarr.amule.model.Download
import amarr.amule.model.DownloadPriority
import amarr.amule.model.DownloadStatus
import amarr.amule.model.SearchFile
import com.google.common.io.BaseEncoding.base32
import com.iukonline.amule.ec.v204.ECClientV204
import com.iukonline.amule.ec.v204.ECCodesV204
import io.ktor.http.*
import io.ktor.util.*
import org.slf4j.Logger
import java.net.Socket

/**
 * ECClients are not thread safe, so we need to synchronize access to it.
 */
@OptIn(ExperimentalStdlibApi::class)
class AmuleClient(
    private val amuleHost: String,
    private val amulePort: Int,
    private val amulePassword: String,
    private val log: Logger
) : AutoCloseable {

    lateinit var client: ECClientV204
    lateinit var socket: Socket

    fun connect() {
        synchronized(this) {
            log.debug("Connecting to amule at {}:{}", amuleHost, amulePort)
            client = ECClientV204()
            client.setClientName("amarr")
            client.setClientVersion("SNAPSHOT")
            client.setPassword(amulePassword)
            socket = Socket(amuleHost, amulePort)
            client.setSocket(socket)
            if (log.isTraceEnabled)
                client.setTracer(System.err)
        }
    }

    override fun close() {
        synchronized(this) {
            log.debug("Closing connection to amule")
            socket.close()
        }
    }

    private val searchCache = mutableMapOf<String, SearchResult>()

    fun search(query: String): List<SearchFile> {
        synchronized(this) {
            val cachedResult = getCachedResult(query)
            if (cachedResult != null) {
                log.debug("Returning cached result")
                return cachedResult
            }
            return performSearch(query)
        }
    }

    fun download(magnet: MagnetLink) {
        synchronized(this) {
            log.debug("Downloading magnet link: {}", magnet)
            client.addED2KLink(magnet.toEd2kLink())
        }
    }

    fun stats() = synchronized(this) { client.stats.toString() }

    fun listDownloads() = synchronized(this) {
        client
            .downloadQueue
            .values
            .map { download ->
                Download(
                    hash = download.hash.toHexString(),
                    fileName = download.fileName,
                    ed2kLink = download.ed2kLink,
                    status = DownloadStatus.fromValue(download.status),
                    prio = DownloadPriority.fromValue(download.prio),
                    cat = download.cat,
                    sourceCount = download.sourceCount,
                    metID = download.metID,
                    sourceA4AF = download.sourceA4AF,
                    sourceXfer = download.sourceXfer,
                    sourceNotCurrent = download.sourceNotCurrent,
                    sizeXfer = download.sizeXfer,
                    sizeFull = download.sizeFull,
                    sizeDone = download.sizeDone,
                    speed = download.speed,
                    lastSeenComp = download.lastSeenComp,
                    lastRecv = download.lastRecv,
                )
            }
    }

    fun delete(hashes: List<String>) {
        synchronized(this) {
            hashes.forEach { hash ->
                log.debug("Deleting torrent with hash: {}", hash)
                client.changeDownloadStatus(hex(hash), ECCodesV204.EC_OP_PARTFILE_DELETE)
            }
        }
    }

    private fun performSearch(query: String): List<SearchFile> {
        val searchResponse = client.searchStart(query, null, null, -1, -1, 0, ECCodesV204.EC_SEARCH_GLOBAL)
        log.debug("Search response: {}", searchResponse)
        log.debug("Search in progress...")
        Thread.sleep(10000) // TODO: Find a better way to wait for search to finish
        val searchResults = client.searchGetReults(null)
        log.trace("Result: {}", searchResults)
        log.debug("Search finished")

        return searchResults.resultMap.values
            .map { result ->
                SearchFile(
                    query = query,
                    fileName = result.fileName,
                    hash = result.hash,
                    sizeFull = result.sizeFull,
                    sourceCount = result.sourceCount,
                    sourceXfer = result.sourceXfer,
                    aaa = result.status,
                    raw = result
                )
            }
    }

    private fun getCachedResult(query: String): List<SearchFile>? {
        val searchResult = searchCache[query]
        if (searchResult == null) {
            log.debug("No cached search result found")
            return null
        }
        if (searchResult.ttl < System.currentTimeMillis()) {
            log.debug("Cached search result expired")
            searchCache.remove(query)
            return null
        }
        log.debug("Found cached search result")
        return searchResult.files
    }

    class SearchResult(
        val files: List<SearchFile>,
        val ttl: Long,
    )

}