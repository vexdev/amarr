package amarr.amule

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jamule.AmuleClient
import jamule.response.StatsResponse
import kotlinx.serialization.json.Json

class DebugApiKtTest : StringSpec({
    val amuleClient = mockk<AmuleClient>()

    val sampleStatsResponse = StatsResponse(
        bannedCount = 0,
        buddyIp = null,
        buddyPort = null,
        buddyStatus = StatsResponse.BuddyState.Disconnected,
        connectionState = null,
        downloadOverhead = 0,
        downloadSpeed = 0,
        downloadSpeedLimit = 0,
        ed2kFiles = 0,
        ed2kUsers = 0,
        kadFiles = 0,
        kadFirewalledUdp = false,
        kadIndexedKeywords = 0,
        kadIndexedLoad = 0,
        kadIndexedNotes = 0,
        kadIndexedSources = 0,
        kadIpAddress = "192.168.3.1",
        kadIsRunningInLanMode = false,
        kadNodes = 0,
        kadUsers = 0,
        loggerMessage = emptyList(),
        sharedFileCount = 0,
        totalReceivedBytes = 0,
        totalSentBytes = 0,
        totalSourceCount = 0,
        uploadOverhead = 0,
        uploadQueueLength = 0,
        uploadSpeed = 0,
        uploadSpeedLimit = 0
    )

    "should call amule client" {
        testApplication {
            application {
                debugApi(amuleClient)
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = true
                    })
                }
            }
            every { amuleClient.getStats() } returns Result.success(sampleStatsResponse)
            val response = client.get("/status")
            response.status.value shouldBe 200
            verify { amuleClient.getStats() }
        }
    }

})
