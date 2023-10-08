package amarr.torznab

import amarr.amule.AmuleClient
import amarr.torznab.model.Feed
import amarr.torznab.model.Feed.Channel.Item
import io.ktor.util.logging.*

class FeedBuilder(private val amuleClient: AmuleClient, private val log: Logger) {

    fun buildFeed(query: String, offset: Int, limit: Int): Feed {
        log.debug("Starting search for query: {}, offset: {}, limit: {}", query, offset, limit)
        return buildFeed(amuleClient.search(query), offset, limit)
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

}