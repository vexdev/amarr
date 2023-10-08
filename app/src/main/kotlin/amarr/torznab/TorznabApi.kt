package amarr.torznab

import amarr.torznab.model.Caps
import amarr.torznab.model.Feed
import amarr.torznab.model.Feed.Channel.Item
import amarr.torznab.model.Feed.Channel.Item.Enclosure
import amarr.torznab.model.Feed.Channel.Item.TorznabAttribute
import amarr.torznab.model.Feed.Channel.Response
import com.iukonline.amule.ec.v204.ECClientV204
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*


fun Application.torznabApi(client: ECClientV204) {
    val torznabApi = TorznabApi(client, log)
    routing {
        get("/api") {
            call.application.log.debug("Handling torznab request")
            call.request.queryParameters["t"]?.let {
                when (it) {
                    "caps" -> torznabApi.handleCaps().let { caps ->
                        call.respond(caps)
                    }

                    "search" -> torznabApi.handleSearch(call).let { search ->
                        call.respond(search)
                    }

                    else -> throw IllegalArgumentException("Unknown action: $it")
                }
            } ?: throw IllegalArgumentException("Missing action")
        }
    }
}

class TorznabApi(client: ECClientV204, private val log: Logger) {
    private val feedBuilder = FeedBuilder(client, log)

    fun handleSearch(call: ApplicationCall): Feed {
        val query = call.request.queryParameters["q"].orEmpty()
        val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
        log.debug("Handling search request: {}, {}, {}", query, offset, limit)
        if (query.isBlank()) {
            log.debug("Empty query, returning empty response")
            return EMPTY_QUERY_RESPONSE
        }
        return feedBuilder.buildFeed(query, offset, limit)
    }

    fun handleCaps(): Caps {
        log.debug("Handling caps request")
        return Caps()
    }

    companion object {
        private val EMPTY_QUERY_RESPONSE = Feed(
            channel = Feed.Channel(
                response = Response(offset = 0, total = 1),
                item = listOf(
                    Item(
                        title = "No query provided",
                        enclosure = Enclosure("http://mock.url", 0),
                        attributes = listOf(
                            TorznabAttribute("category", "1"),
                            TorznabAttribute("size", "0")
                        )
                    )
                )
            )
        )
    }
}
