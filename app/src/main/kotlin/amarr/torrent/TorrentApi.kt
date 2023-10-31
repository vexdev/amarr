package amarr.torrent

import amarr.category.CategoryStore
import amarr.torrent.model.Category
import amarr.torrent.model.Preferences
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jamule.AmuleClient

fun Application.torrentApi(amuleClient: AmuleClient, categoryStore: CategoryStore, finishedPath: String) {
    val service = TorrentService(amuleClient, categoryStore, finishedPath, log)
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
            call.respond(Preferences())
        }
        post("/api/v2/torrents/add") {
            val params = call.receiveParameters()
            val urls = params["urls"]?.split("\n")?.filterNot { it.isBlank() }
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
            val category = Category(params["category"]!!, params["savePath"] ?: "")
            call.application.log.debug("Received create category request with category: {}", category)
            service.addCategory(category)
            call.respondText("Ok.")
        }
        get("/api/v2/torrents/categories") {
            call.respond(service.getCategories())
        }
        get("/api/v2/torrents/info") {
            val category = call.request.queryParameters["category"]
            call.respond(service.getTorrentInfo(category))
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
        get("/api/v2/torrents/files") {
            val hash = call.request.queryParameters["hash"]!!
            call.application.log.debug("Received get files request with hash: {}", hash)
            val response = listOf(service.getFile(hash))
            call.respond(response)
        }
        get("/api/v2/torrents/properties") {
            val hash = call.request.queryParameters["hash"]!!
            call.application.log.debug("Received get properties request with hash: {}", hash)
            val response = service.getTorrentProperties(hash)
            call.respond(response)
        }
    }
}
