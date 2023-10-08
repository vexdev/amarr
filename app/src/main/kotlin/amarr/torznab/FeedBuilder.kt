package amarr.torznab

import amarr.AMARR_URL
import amarr.amule.AmuleClient
import amarr.amule.model.SearchFile
import amarr.torznab.model.Feed
import amarr.torznab.model.Feed.Channel.Item
import io.ktor.http.*
import io.ktor.util.logging.*

class FeedBuilder(private val amuleClient: AmuleClient, private val log: Logger) {

    fun buildFeed(query: String, offset: Int, limit: Int): Feed {
        log.debug("Starting search for query: {}, offset: {}, limit: {}", query, offset, limit)
        return buildFeed(amuleClient.search(query), offset, limit)
    }

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
                            url = AMARR_URL +
                                    "/download" +
                                    "?query=${result.query.encodeURLParameter()}" +
                                    "&hash=${result.hash.encodeURLParameter()}" +
                                    "&size=${result.sizeFull}",
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
        )
    )

}