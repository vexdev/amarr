package amarr.torznab

import amarr.torznab.indexer.AmuleIndexer
import amarr.torznab.indexer.Indexer
import amarr.torznab.indexer.ddunlimitednet.DdunlimitednetIndexer
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nl.adaptivity.xmlutil.serialization.XML


fun Application.torznabApi(amuleIndexer: AmuleIndexer, ddunlimitednetIndexer: DdunlimitednetIndexer) {
    routing {
        // Kept for legacy reasons
        get("/api") {
            call.handleRequests(amuleIndexer)
        }
        get("/indexer/amule/api") {
            call.handleRequests(amuleIndexer)
        }
        get("indexer/ddunlimitednet/api") {
            call.handleRequests(ddunlimitednetIndexer)
        }
    }
}

private suspend fun ApplicationCall.handleRequests(indexer: Indexer) {
    application.log.debug("Handling torznab request")
    val xmlFormat = XML // This API uses XML instead of JSON
    request.queryParameters["t"]?.let {
        when (it) {
            "caps" -> {
                application.log.debug("Handling caps request")
                respondText(xmlFormat.encodeToString(indexer.capabilities()), contentType = ContentType.Application.Xml)
            }

            "search" -> {
                val query = request.queryParameters["q"].orEmpty()
                val offset = request.queryParameters["offset"]?.toIntOrNull() ?: 0
                val limit = request.queryParameters["limit"]?.toIntOrNull() ?: 100
                application.log.debug("Handling search request: {}, {}, {}", query, offset, limit)
                respondText(
                    xmlFormat.encodeToString(indexer.search(query, offset, limit)),
                    contentType = ContentType.Application.Xml
                )
            }

            else -> throw IllegalArgumentException("Unknown action: $it")
        }
    } ?: throw IllegalArgumentException("Missing action")
}