package amarr.torznab

import amarr.torznab.indexer.AmuleIndexer
import amarr.torznab.indexer.Indexer
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.torznabApi(amuleIndexer: AmuleIndexer) {
    routing {
        get("/api") {
            call.handleRequests(amuleIndexer)
        }
    }
}

private suspend fun ApplicationCall.handleRequests(indexer: Indexer) {
    application.log.debug("Handling torznab request")
    request.queryParameters["t"]?.let {
        when (it) {
            "caps" -> {
                application.log.debug("Handling caps request")
                respond(indexer.capabilities())
            }

            "search" -> {
                val query = request.queryParameters["q"].orEmpty()
                val offset = request.queryParameters["offset"]?.toIntOrNull() ?: 0
                val limit = request.queryParameters["limit"]?.toIntOrNull() ?: 100
                application.log.debug("Handling search request: {}, {}, {}", query, offset, limit)
                respond(indexer.search(query, offset, limit))
            }

            else -> throw IllegalArgumentException("Unknown action: $it")
        }
    } ?: throw IllegalArgumentException("Missing action")
}