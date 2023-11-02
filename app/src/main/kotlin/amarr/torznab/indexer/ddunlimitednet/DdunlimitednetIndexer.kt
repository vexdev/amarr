package amarr.torznab.indexer.ddunlimitednet

import amarr.MagnetLink
import amarr.torznab.indexer.Indexer
import amarr.torznab.indexer.ThrottledException
import amarr.torznab.indexer.UnauthorizedException
import amarr.torznab.model.Caps
import amarr.torznab.model.Feed
import org.slf4j.Logger

class DdunlimitednetIndexer(
    private val client: DdunlimitednetClient,
    private val log: Logger
) : Indexer {

    override suspend fun search(query: String, offset: Int, limit: Int): Feed {
        log.info("Searching for query: {}", query)
        val result = client.search(query).recover { error ->
            when (error) {
                is UnauthorizedException -> {
                    log.info("Unauthorized, logging in...")
                    client.login()
                    client.search(query).getOrThrow()
                }

                is ThrottledException -> {
                    log.info("Throttled, returning 403...")
                    throw error
                }

                else -> throw error
            }
        }
        return linksToFeed(result.getOrThrow())
    }

    override suspend fun capabilities(): Caps = Caps() // TODO: Configure caps

    private fun linksToFeed(links: List<String>): Feed {
        log.info("Found {} links", links.size)
        log.trace("Links: {}", links)
        return Feed(
            channel = Feed.Channel(
                response = Feed.Channel.Response(
                    offset = 0,
                    total = links.size
                ),
                item = links
                    .mapNotNull { link -> runCatching { MagnetLink.fromEd2k(link) }.getOrNull() }
                    .map { link ->
                        Feed.Channel.Item(
                            title = link.name,
                            enclosure = Feed.Channel.Item.Enclosure(
                                url = link.toString(),
                                length = 0
                            ),
                            attributes = listOf(
                                Feed.Channel.Item.TorznabAttribute("category", "1"),
                                Feed.Channel.Item.TorznabAttribute("seeders", "1"),
                                Feed.Channel.Item.TorznabAttribute("peers", "1"),
                                Feed.Channel.Item.TorznabAttribute("size", link.size.toString())
                            )
                        )
                    }
            )
        )
    }

}