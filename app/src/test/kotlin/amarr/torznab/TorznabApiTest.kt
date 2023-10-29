package amarr.torznab

import amarr.torznab.indexer.AmuleIndexer
import amarr.torznab.model.Caps
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.ktor.client.request.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class TorznabApiTest : StringSpec({
    val amuleIndexer = mockk<AmuleIndexer>()

    "should throw exception when missing action" {
        testApplication {
            application {
                torznabApi(amuleIndexer)
            }
            shouldThrow<IllegalArgumentException> { client.get("/api") }
        }
    }

    "should throw exception on unknown action" {
        testApplication {
            application {
                torznabApi(amuleIndexer)
            }
            shouldThrow<IllegalArgumentException> { client.get("/api?t=unknown") }
        }
    }

    "should get capabilities from amule indexer when called on /api" {
        testApplication {
            application {
                torznabApi(amuleIndexer)
            }
            every { amuleIndexer.capabilities() } returns Caps()
            client.get("/api?t=caps")
            verify { amuleIndexer.capabilities() }
        }
    }

    "should pass query, offset and limits to amule indexer when called on /api" {
        testApplication {
            application {
                torznabApi(amuleIndexer)
            }
            every { amuleIndexer.search("test", 0, 100) } returns mockk()
            client.get("/api?t=search&q=test&offset=0&limit=100")
            verify { amuleIndexer.search("test", 0, 100) }
        }
    }
})