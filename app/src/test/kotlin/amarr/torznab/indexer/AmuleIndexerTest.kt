package amarr.torznab.indexer

import amarr.MagnetLink
import amarr.torznab.model.Feed.Channel.Item.TorznabAttribute
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jamule.AmuleClient
import jamule.response.SearchResultsResponse
import jamule.response.SearchResultsResponse.SearchFile
import org.slf4j.LoggerFactory

class AmuleIndexerTest : StringSpec({
    val mockClient = mockk<AmuleClient>()
    val logger = LoggerFactory.getLogger(AmuleIndexerTest::class.java)

    "should return single category in capabilities" {
        val indexer = AmuleIndexer(mockClient, logger)
        val capabilities = indexer.capabilities()
        capabilities.categories.category.size shouldBe 1
        capabilities.categories.category[0].name shouldBe "All"
        capabilities.categories.category[0].id shouldBe 1
    }

    "when empty queried should return only one result within that category" {
        val indexer = AmuleIndexer(mockClient, logger)
        val results = indexer.search("", 0, 1000, listOf())
        results.channel.response.total shouldBe 1
        results.channel.response.offset shouldBe 0
        results.channel.item.size shouldBe 1
        val item = results.channel.item[0]
        item.title shouldBe "No query provided"
        item.enclosure.url shouldBe "http://mock.url"
        item.enclosure.length shouldBe 0
        item.attributes.size shouldBe 2
        item.attributes[0].name shouldBe "category"
        item.attributes[0].value shouldBe "1"
        item.attributes[1].name shouldBe "size"
        item.attributes[1].value shouldBe "0"
        verify { mockClient wasNot Called }
    }

    "when queried calls amule client" {
        val searchFile = SearchFile(
            fileName = "test",
            hash = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
            sizeFull = 1000,
            completeSourceCount = 1,
            sourceCount = 2,
            downloadStatus = SearchResultsResponse.SearchFileDownloadStatus.NEW,
        )
        every { mockClient.searchSync(any()) } returns Result.success(SearchResultsResponse(listOf(searchFile)))
        val indexer = AmuleIndexer(mockClient, logger)
        val result = indexer.search("test", 0, 1000, listOf())
        verify { mockClient.searchSync("test") }
        result.channel.response.total shouldBe 1
        result.channel.response.offset shouldBe 0
        result.channel.item.size shouldBe 1
        val item = result.channel.item[0]
        item.title shouldBe "test"
        item.enclosure.url shouldBe MagnetLink.forAmarr(searchFile.hash, "test", searchFile.sizeFull).toString()
        item.enclosure.length shouldBe 1000
        item.attributes.size shouldBe 4
        item.attributes shouldContain TorznabAttribute("category", "1")
        item.attributes shouldContain TorznabAttribute("size", "1000")
        item.attributes shouldContain TorznabAttribute("seeders", "1")
        item.attributes shouldContain TorznabAttribute("peers", "2")
    }

})
