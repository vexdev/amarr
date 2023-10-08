package amarr.amule

import amarr.AMARR_URL
import amarr.torznab.model.Feed.Channel.Item
import com.iukonline.amule.ec.v204.ECClientV204
import com.iukonline.amule.ec.v204.ECCodesV204
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.logging.*

class AmuleClient(private val client: ECClientV204, private val log: Logger) {
    private val searchCache = mutableMapOf<String, SearchResult>()

    fun search(query: String): List<Item> {
        val cachedResult = getCachedResult(query)
        if (cachedResult != null) {
            log.debug("Returning cached result")
            return cachedResult
        }
        return performSearch(query)
    }

    fun stats() = client.stats.toString()

    private fun performSearch(query: String): List<Item> {
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
                        Item.TorznabAttribute("category", "1"),
                        Item.TorznabAttribute("seeders", result.sourceCount.toString()),
                        Item.TorznabAttribute("peers", result.sourceCount.toString()),
                        Item.TorznabAttribute("size", result.sizeFull.toString())
                    )
                )
            }
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

    class SearchResult(
        val items: List<Item>,
        val ttl: Long,
    )
}