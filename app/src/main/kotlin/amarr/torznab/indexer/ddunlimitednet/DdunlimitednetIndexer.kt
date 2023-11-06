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

    override suspend fun search(query: String, offset: Int, limit: Int, cat: List<Int>): Feed {
        log.info("Searching for query: `{}` in categories: `{}`", query, cat)
        // Here "tutto" is simply a word that will match something in all supported categories
        val cleanQuery = query.ifEmpty { "tutto" }
        val result = client.search(cleanQuery, cat).recover { error ->
            when (error) {
                is UnauthorizedException -> {
                    log.info("Unauthorized, logging in...")
                    client.login()
                    client.search(cleanQuery, cat).getOrThrow()
                }

                is ThrottledException -> {
                    log.info("Throttled, returning 403...")
                    throw error
                }

                else -> throw error
            }
        }
        return linksToFeed(result.getOrThrow(), cat)
    }

    // TODO: Pagination
    override suspend fun capabilities(): Caps = Caps(
        categories = Caps.Categories(
            category = listOf(
                Caps.Categories.Category(
                    id = 1577,
                    name = "SerieTV",
                ),
                Caps.Categories.Category(
                    id = 1572,
                    name = "Movies",
                )
            )
        )
    )

    private fun linksToFeed(links: List<String>, cat: List<Int>): Feed {
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
                    .mapIndexed { idx, link ->
                        // TODO: This is horrible, but we do not yet have the categories implemented in the scraper.
                        val currentCategory = if (cat.isEmpty()) "1" else cat[idx % cat.size].toString()
                        Feed.Channel.Item(
                            title = link.name,
                            enclosure = Feed.Channel.Item.Enclosure(
                                url = link.toString(),
                                length = 0
                            ),
                            attributes = listOf(
                                Feed.Channel.Item.TorznabAttribute("category", currentCategory),
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