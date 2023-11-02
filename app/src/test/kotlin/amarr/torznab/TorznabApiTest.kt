package amarr.torznab

import amarr.torznab.indexer.AmuleIndexer
import amarr.torznab.indexer.ddunlimitednet.DdunlimitednetIndexer
import amarr.torznab.model.Caps
import amarr.torznab.model.Feed
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.ktor.client.request.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class TorznabApiTest : StringSpec({
    val amuleIndexer = mockk<AmuleIndexer>()
    val ddunlimitednetIndexer = mockk<DdunlimitednetIndexer>()

    "should throw exception when missing action" {
        testApplication {
            application {
                torznabApi(amuleIndexer, ddunlimitednetIndexer)
            }
            shouldThrow<IllegalArgumentException> { client.get("/api") }
        }
    }

    "should throw exception on unknown action" {
        testApplication {
            application {
                torznabApi(amuleIndexer, ddunlimitednetIndexer)
            }
            shouldThrow<IllegalArgumentException> { client.get("/api?t=unknown") }
        }
    }

    "should get capabilities from amule indexer when called on /api" {
        testApplication {
            application {
                torznabApi(amuleIndexer, ddunlimitednetIndexer)
            }
            coEvery { amuleIndexer.capabilities() } returns Caps()
            client.get("/api?t=caps")
            coVerify { amuleIndexer.capabilities() }
        }
    }

    "should pass query, offset and limits to amule indexer when called on /api" {
        testApplication {
            application {
                torznabApi(amuleIndexer, ddunlimitednetIndexer)
            }
            coEvery {
                amuleIndexer.search(
                    "test",
                    0,
                    100
                )
            } returns Feed(
                channel = Feed.Channel(
                    response = Feed.Channel.Response(offset = 0, total = 0),
                    item = emptyList()
                )
            )
            client.get("/api?t=search&q=test&offset=0&limit=100")
            coVerify { amuleIndexer.search("test", 0, 100) }
        }
    }
})