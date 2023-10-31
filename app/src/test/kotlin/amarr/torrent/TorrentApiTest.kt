package amarr.torrent

import amarr.MagnetLink
import amarr.category.CategoryStore
import amarr.torrent.model.Category
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import jamule.AmuleClient
import jamule.model.AmuleTransferringFile
import jamule.model.DownloadCommand
import jamule.model.FileStatus
import kotlinx.serialization.json.Json

class TorrentApiTest : StringSpec({
    val amuleClient = mockk<AmuleClient>()
    val categoryStore = MemoryCategoryStore()
    val testMagnetHash = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
    val testMagnetLink = MagnetLink.forAmarr(testMagnetHash, "test", 1)
    val finishedPath = "/finished"

    beforeAny {
        clearAllMocks()
    }

    "should get preferences" {
        testApplication {
            application {
                torrentApi(amuleClient, categoryStore, finishedPath)
                configureForTest()
            }
            client.get("/api/v2/app/preferences").apply {
                this.status shouldBe HttpStatusCode.OK
            }
        }
    }

    "should get api version" {
        testApplication {
            application {
                torrentApi(amuleClient, categoryStore, finishedPath)
                configureForTest()
            }
            client.get("/api/v2/app/webapiVersion").apply {
                this.status shouldBe HttpStatusCode.OK
            }
        }
    }

    "should allow login" {
        testApplication {
            application {
                torrentApi(amuleClient, categoryStore, finishedPath)
                configureForTest()
            }
            client.submitForm(formParameters = Parameters.build {
                append("username", "test")
                append("password", "test")
            }, url = "/api/v2/auth/login").apply {
                this.status shouldBe HttpStatusCode.OK
            }
        }
    }

    "should add torrent" {
        testApplication {
            application {
                torrentApi(amuleClient, categoryStore, finishedPath)
                configureForTest()
            }
            val urls = listOf(testMagnetLink.toString())
            val ed2k = testMagnetLink.toEd2kLink()
            every { amuleClient.downloadEd2kLink(ed2k) } returns Result.success(Unit)
            client.submitForm(formParameters = Parameters.build {
                appendAll("urls", urls)
                append("category", "test")
                append("paused", "test")
            }, url = "/api/v2/torrents/add").apply {
                this.status shouldBe HttpStatusCode.OK
            }
        }
    }

    "should get categories" {
        testApplication {
            application {
                torrentApi(amuleClient, categoryStore, finishedPath)
                configureForTest()
            }
            client.get("/api/v2/torrents/categories").apply {
                this.status shouldBe HttpStatusCode.OK
            }
        }
    }

    "should create category" {
        testApplication {
            application {
                torrentApi(amuleClient, categoryStore, finishedPath)
                configureForTest()
            }
            client.submitForm(formParameters = Parameters.build {
                append("category", "test")
                append("savePath", "test")
            }, url = "/api/v2/torrents/createCategory").apply {
                this.status shouldBe HttpStatusCode.OK
            }
        }
    }

    "should delete torrent" {
        testApplication {
            application {
                torrentApi(amuleClient, categoryStore, finishedPath)
                configureForTest()
            }
            every {
                amuleClient.sendDownloadCommand(testMagnetHash, DownloadCommand.DELETE)
            } returns Result.success(Unit)
            client.submitForm(formParameters = Parameters.build {
                append("hashes", testMagnetLink.amuleHexHash())
                append("deleteFiles", "test")
            }, url = "/api/v2/torrents/delete").apply {
                this.status shouldBe HttpStatusCode.OK
            }
        }
    }

    "should get files" {
        testApplication {
            application {
                torrentApi(amuleClient, categoryStore, finishedPath)
                configureForTest()
            }
            amuleClient.addToDownloadQueue(testMagnetLink)
            every { amuleClient.getSharedFiles() } returns Result.success(emptyList())
            client.get {
                url("/api/v2/torrents/files")
                parameter("hash", testMagnetLink.amuleHexHash())
            }.apply {
                this.status shouldBe HttpStatusCode.OK
            }
        }
    }

    "should get info" {
        testApplication {
            application {
                torrentApi(amuleClient, categoryStore, finishedPath)
                configureForTest()
            }
            amuleClient.addToDownloadQueue(testMagnetLink)
            every { amuleClient.getSharedFiles() } returns Result.success(emptyList())
            client.get {
                url("/api/v2/torrents/info")
                parameter("category", "test")
            }.apply {
                this.status shouldBe HttpStatusCode.OK
            }
        }
    }

    "should get properties" {
        testApplication {
            application {
                torrentApi(amuleClient, categoryStore, finishedPath)
                configureForTest()
            }
            amuleClient.addToDownloadQueue(testMagnetLink)
            every { amuleClient.getSharedFiles() } returns Result.success(emptyList())
            client.get {
                url("/api/v2/torrents/properties")
                parameter("hash", testMagnetLink.amuleHexHash())
            }.apply {
                this.status shouldBe HttpStatusCode.OK
            }
        }
    }
})

private fun AmuleClient.addToDownloadQueue(magnetLink: MagnetLink) {
    every { this@addToDownloadQueue.getDownloadQueue() } returns Result.success(
        listOf(
            MockTransferringFile(
                fileHashHexString = magnetLink.amuleHexHash(),
                fileName = magnetLink.name,
                sizeFull = magnetLink.size,
            )
        )
    )
}

private fun Application.configureForTest() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
            encodeDefaults = true
        })
    }
}

private class MemoryCategoryStore() : CategoryStore {

    private val categories = mutableSetOf<Category>()
    private val hashes = mutableMapOf<String, String>()

    override fun store(category: String, hash: String) {
        hashes[hash] = category
    }

    override fun getCategory(hash: String): String? {
        return hashes[hash]
    }

    override fun delete(hash: String) {
        hashes.remove(hash)
    }

    override fun addCategory(category: Category) {
        categories.add(category)
    }

    override fun getCategories(): Set<Category> {
        return categories
    }

}

private data class MockTransferringFile(
    override val fileHashHexString: String? = null,
    override val partMetID: Short? = 0,
    override val sizeXfer: Long? = 0,
    override val sizeDone: Long? = 0,
    override val fileStatus: FileStatus = FileStatus.UNKNOWN,
    override val stopped: Boolean = false,
    override val sourceCount: Short = 0,
    override val sourceNotCurrCount: Short = 0,
    override val sourceXferCount: Short = 0,
    override val sourceCountA4AF: Short = 0,
    override val speed: Long? = 0,
    override val downPrio: Byte = 0,
    override val fileCat: Long = 0,
    override val lastSeenComplete: Long = 0,
    override val lastDateChanged: Long = 0,
    override val downloadActiveTime: Int = 0,
    override val availablePartCount: Short = 0,
    override val a4AFAuto: Boolean = false,
    override val hashingProgress: Boolean = false,
    override val getLostDueToCorruption: Long = 0,
    override val getGainDueToCompression: Long = 0,
    override val totalPacketsSavedDueToICH: Int = 0,
    override val fileName: String? = null,
    override val filePath: String? = null,
    override val sizeFull: Long? = 0,
    override val fileEd2kLink: String? = null,
    override val upPrio: Byte = 0,
    override val getRequests: Short = 0,
    override val getAllRequests: Int = 0,
    override val getAccepts: Short = 0,
    override val getAllAccepts: Int = 0,
    override val getXferred: Long = 0,
    override val getAllXferred: Long = 0,
    override val getCompleteSourcesLow: Short = 0,
    override val getCompleteSourcesHigh: Short = 0,
    override val getCompleteSources: Short = 0,
    override val getOnQueue: Short = 0,
    override val getComment: String? = null,
    override val getRating: Byte? = 0,
) : AmuleTransferringFile
