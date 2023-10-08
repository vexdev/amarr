package amarr.tracker

import amarr.amule.AmuleClient
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*

fun Application.trackerApi(client: AmuleClient) {
    val trackerApi = TrackerApi(client, log)
    routing {
        get("/download") {
            val hash = call.request.queryParameters["hash"].orEmpty()
            val query = call.request.queryParameters["query"].orEmpty()
            call.application.log.debug("Handling download request: {}, {}", hash, query)
            trackerApi.handleDownload(hash, query)
            call.respondText("Hello World!")
        }
    }
}

class TrackerApi(client: AmuleClient, private val log: Logger) {
    fun handleDownload(hash: String, query: String) {

    }

}