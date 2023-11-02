package amarr.torznab.indexer

import amarr.MagnetLink
import amarr.torznab.model.Caps
import amarr.torznab.model.Feed
import amarr.torznab.model.Feed.Channel.Item
import io.ktor.util.logging.*
import jamule.AmuleClient
import jamule.response.SearchResultsResponse.SearchFile

class AmuleIndexer(private val amuleClient: AmuleClient, private val log: Logger) : Indexer {

    override suspend fun search(query: String, offset: Int, limit: Int): Feed {
        log.debug("Starting search for query: {}, offset: {}, limit: {}", query, offset, limit)
        if (query.isBlank()) {
            log.debug("Empty query, returning empty response")
            return EMPTY_QUERY_RESPONSE
        }
        return buildFeed(amuleClient.searchSync(query).getOrThrow().files, offset, limit)
    }

    override suspend fun capabilities(): Caps = Caps()

    private fun buildFeed(items: List<SearchFile>, offset: Int, limit: Int) = Feed(
        channel = Feed.Channel(
            response = Feed.Channel.Response(
                offset = offset,
                total = items.size
            ),
            item = items
                .drop(offset)
                .take(limit)
                .map { result ->
                    Item(
                        title = result.fileName,
                        enclosure = Item.Enclosure(
                            url = MagnetLink.forAmarr(result.hash, result.fileName, result.sizeFull).toString(),
                            length = result.sizeFull
                        ),
                        attributes = listOf(
                            Item.TorznabAttribute("category", "1"),
                            Item.TorznabAttribute("seeders", result.completeSourceCount.toString()),
                            Item.TorznabAttribute("peers", result.sourceCount.toString()),
                            Item.TorznabAttribute("size", result.sizeFull.toString())
                        )
                    )
                }
        )
    )

    companion object {
        private val EMPTY_QUERY_RESPONSE = Feed(
            channel = Feed.Channel(
                response = Feed.Channel.Response(offset = 0, total = 1),
                item = listOf(
                    Item(
                        title = "No query provided",
                        enclosure = Item.Enclosure("http://mock.url", 0),
                        attributes = listOf(
                            Item.TorznabAttribute("category", "1"),
                            Item.TorznabAttribute("size", "0")
                        )
                    )
                )
            )
        )
    }

}