package amarr.torznab

import amarr.torznab.indexer.AmuleIndexer
import amarr.torznab.indexer.Indexer
import amarr.torznab.indexer.ThrottledException
import amarr.torznab.indexer.UnauthorizedException
import amarr.torznab.indexer.ddunlimitednet.DdunlimitednetIndexer
import amarr.torznab.model.Feed
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.core.XmlVersion
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
    val xmlFormat = XML {
        xmlDeclMode = XmlDeclMode.Charset
        xmlVersion = XmlVersion.XML10
    } // This API uses XML instead of JSON
    request.queryParameters["t"]?.let {
        when (it) {
            "caps" -> {
                application.log.debug("Handling caps request")
                respondText(xmlFormat.encodeToString(indexer.capabilities()), contentType = ContentType.Application.Xml)
            }

            "tvsearch" -> performSearch(indexer, xmlFormat, SearchMode.Tv)
            "movie" -> performSearch(indexer, xmlFormat, SearchMode.Default)
            "search" -> performSearch(indexer, xmlFormat)

            else -> throw IllegalArgumentException("Unknown action: $it")
        }
    } ?: throw IllegalArgumentException("Missing action")
}

private suspend fun ApplicationCall.performSearch(indexer: Indexer, xmlFormat: XML) {
    performSearch(indexer, xmlFormat, SearchMode.Default)
}

private suspend fun ApplicationCall.performSearch(indexer: Indexer, xmlFormat: XML, mode: SearchMode) {
    val query = request.queryParameters["q"].orEmpty()
    val offset = request.queryParameters["offset"]?.toIntOrNull() ?: 0
    val limit = request.queryParameters["limit"]?.toIntOrNull() ?: 100
    val cat = request.queryParameters["cat"]?.split(",")?.map { cat -> cat.toInt() } ?: emptyList()
    val queries = searchQueries(query, mode)
    application.log.debug("Handling search request: {}, {}, {}, {}", queries, offset, limit, cat)
    try {
        respondText(
            xmlFormat.encodeToString(performQueries(indexer, queries, offset, limit, cat)),
            contentType = ContentType.Application.Xml
        )
    } catch (e: ThrottledException) {
        application.log.warn("Throttled, returning 403")
        respondText("You are being throttled. Retry in a few minutes.", status = HttpStatusCode.Forbidden)
    } catch (e: UnauthorizedException) {
        application.log.warn("Unauthorized, returning 401")
        respondText("Unauthorized, check your credentials.", status = HttpStatusCode.Unauthorized)
    }
}

private suspend fun ApplicationCall.performQueries(
    indexer: Indexer,
    queries: List<String>,
    offset: Int,
    limit: Int,
    cat: List<Int>
): Feed {
    if (queries.size == 1) {
        return indexer.search(queries.single(), offset, limit, cat)
    }

    val rawLimit = offset + limit
    val items = queries
        .flatMap { query -> indexer.search(query, 0, rawLimit, cat).channel.item }
        .distinctBy { item -> item.enclosure.url }
    return Feed(
        channel = Feed.Channel(
            response = Feed.Channel.Response(
                offset = offset,
                total = items.size
            ),
            item = items.drop(offset).take(limit)
        )
    )
}

private fun ApplicationCall.searchQueries(query: String, mode: SearchMode): List<String> {
    if (mode != SearchMode.Tv || query.isBlank()) {
        return listOf(query)
    }
    val season = request.queryParameters["season"]?.toIntOrNull() ?: return listOf(query)
    val episode = request.queryParameters["episode"]?.toIntOrNull() ?: return listOf(query)
    val paddedSeason = season.toString().padStart(2, '0')
    val paddedEpisode = episode.toString().padStart(2, '0')
    return listOf(
        "$query S${paddedSeason}E$paddedEpisode",
        "$query ${season}x$paddedEpisode",
        "$query ${season}$paddedEpisode"
    )
}

private enum class SearchMode {
    Default,
    Tv
}
