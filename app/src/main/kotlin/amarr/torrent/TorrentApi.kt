package amarr.torrent

import amarr.amule.AmuleClient
import amarr.torrent.model.Preferences
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Application.torrentApi(amuleClient: AmuleClient) {
    val service = TorrentService(amuleClient, log)
    val format = Json { encodeDefaults = true }
    routing {
        get("/api/v2/app/webapiVersion") {
            call.respondText("2.8.19") // Emulating qBittorrent API version 2.8.19
        }
        post("/api/v2/auth/login") {
            val params = call.receiveParameters()
            val username = params["username"]
            val password = params["password"]
            // TODO: Implement some kind of authentication
            call.respondText("Ok.")
        }
        get("/api/v2/app/preferences") {
            call.respondText(format.encodeToString(Preferences()), ContentType.Application.Json)
        }
        post("/api/v2/torrents/add") {
            val params = call.receiveParameters()
            val urls = params["urls"]!!.split("\n")
            val category = params["category"]
            val paused = params["paused"]
            call.application.log.debug(
                "Received add torrent request with urls: {}, category: {}, paused: {}",
                urls,
                category,
                paused
            )
            service.addTorrent(urls, category, paused)
            call.respondText("Ok.")
        }
        post("/api/v2/torrents/createCategory") {
            val params = call.receiveParameters()
            val category = params["category"]!!
            call.application.log.debug("Received create category request with category: {}", category)
            service.addCategory(category)
            call.respondText("Ok.")
        }
        get("/api/v2/torrents/categories") {
            call.respondText(format.encodeToString(service.getCategories()), ContentType.Application.Json)
        }
        get("/api/v2/torrents/info") {
            val category = call.request.queryParameters["category"]
            call.respondText(format.encodeToString(service.getTorrentInfo(category)), ContentType.Application.Json)
        }
        post("/api/v2/torrents/delete") {
            val params = call.receiveParameters()
            val hashes = params["hashes"]!!.split("|")
            val deleteFiles = params["deleteFiles"]
            call.application.log.debug(
                "Received delete torrent request with hashes: {}, deleteFiles: {}",
                hashes,
                deleteFiles
            )
            if (hashes.size == 1 && hashes[0] == "all")
                service.deleteAllTorrents(deleteFiles)
            else service.deleteTorrent(hashes, deleteFiles)
            call.respondText("Ok.")
        }
    }
}
