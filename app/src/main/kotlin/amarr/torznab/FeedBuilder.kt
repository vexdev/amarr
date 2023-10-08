package amarr.torznab

import amarr.AMARR_URL
import amarr.torznab.model.Feed
import amarr.torznab.model.Feed.Channel.Item
import amarr.torznab.model.Feed.Channel.Item.TorznabAttribute
import com.iukonline.amule.ec.v204.ECClientV204
import com.iukonline.amule.ec.v204.ECCodesV204
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.logging.*

class FeedBuilder(private val client: ECClientV204, private val log: Logger) {
    private val searchCache = mutableMapOf<String, SearchResult>()
    fun buildFeed(query: String, offset: Int, limit: Int): Feed {
        log.debug("Starting search for query: {}, offset: {}, limit: {}", query, offset, limit)

        val cachedResult = getCachedResult(query)
        if (cachedResult != null) {
            log.debug("Returning cached result")
            return buildFeed(cachedResult, offset, limit)
        }
        val result = search(query)
        searchCache[query] = SearchResult(
            items = result,
            ttl = (System.currentTimeMillis() + 1000 * 60 * 60) // 1 hour
        )
        return buildFeed(result, offset, limit)
    }

    private fun getCachedResult(query: String): List<Item>? {
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
        return searchResult.items
    }

    private fun buildFeed(items: List<Item>, offset: Int, limit: Int) = Feed(
        channel = Feed.Channel(
            response = Feed.Channel.Response(
                offset = offset,
                total = items.size
            ),
            item = items
                .drop(offset)
                .take(limit)
        )
    )

    private fun search(query: String): List<Item> {
        val searchResponse = client.searchStart(query, null, null, -1, -1, 0, ECCodesV204.EC_SEARCH_GLOBAL)
        log.debug("Search response: {}", searchResponse)
        log.debug("Search in progress...")
        Thread.sleep(10000) // TODO: Find a better way to wait for search to finish
        val searchResults = client.searchGetReults(null)
        log.trace("Result: {}", searchResults)
        log.debug("Search finished")

        return searchResults.resultMap.values
            .map { result ->
                Item(
                    title = result.fileName,
                    enclosure = Item.Enclosure(
                        url = AMARR_URL +
                                "/download" +
                                "?query=${query.encodeURLParameter()}" +
                                "&hash=${result.hash.encodeBase64().encodeURLParameter()}",
                        length = result.sizeFull
                    ),
                    attributes = listOf(
                        TorznabAttribute("category", "1"),
                        TorznabAttribute("seeders", result.sourceCount.toString()),
                        TorznabAttribute("peers", result.sourceCount.toString()),
                        TorznabAttribute("size", result.sizeFull.toString())
                    )
                )
            }
    }

    class SearchResult(
        val items: List<Item>,
        val ttl: Long,
    )
}