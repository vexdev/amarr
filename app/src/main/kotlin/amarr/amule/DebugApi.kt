package amarr.amule

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jamule.AmuleClient
import jamule.response.StatsResponse
import kotlinx.serialization.Serializable

fun Application.debugApi(client: AmuleClient) {
    routing {
        get("/status") {
            call.respond(client.getStats().getOrThrow().let { StatusResponse.fromStatsResponse(it) })
        }
    }
}

@Serializable
data class StatusResponse(
    val bannedCount: Long = 0,
    val buddyIp: String? = null,
    val buddyPort: Short? = null,
    val buddyStatus: String? = null,
    val connectionStatus: ConnectionStatus? = ConnectionStatus(),
    val downloadOverhead: Long = 0,
    val downloadSpeed: Long = 0,
    val downloadSpeedLimit: Long = 0,
    val ed2kFiles: Long = 0,
    val ed2kUsers: Long = 0,
    val kadFiles: Long = 0,
    val kadFirewalledUdp: Boolean? = null,
    val kadIndexedKeywords: Long? = null,
    val kadIndexedLoad: Long? = null,
    val kadIndexedNotes: Long? = null,
    val kadIndexedSources: Long? = null,
    val kadIpAddress: String? = null,
    val kadIsRunningInLanMode: Boolean? = null,
    val kadNodes: Long = 0,
    val kadUsers: Long = 0,
    val loggerMessage: List<String> = emptyList(),
    val sharedFileCount: Long = 0,
    val totalReceivedBytes: Long = 0,
    val totalSentBytes: Long = 0,
    val totalSourceCount: Long = 0,
    val uploadOverhead: Long = 0,
    val uploadQueueLength: Long = 0,
    val uploadSpeed: Long = 0,
    val uploadSpeedLimit: Long = 0
) {
    companion object {
        fun fromStatsResponse(statsResponse: StatsResponse) =
            StatusResponse(
                bannedCount = statsResponse.bannedCount,
                buddyIp = statsResponse.buddyIp,
                buddyPort = statsResponse.buddyPort?.toShort(),
                buddyStatus = statsResponse.buddyStatus?.name,
                connectionStatus = ConnectionStatus(
                    clientId = statsResponse.connectionState?.clientId,
                    ed2kConnected = statsResponse.connectionState?.ed2kConnected,
                    ed2kConnecting = statsResponse.connectionState?.ed2kConnecting,
                    ed2kId = statsResponse.connectionState?.ed2kId,
                    kadConnected = statsResponse.connectionState?.kadConnected,
                    kadFirewalled = statsResponse.connectionState?.kadFirewalled,
                    kadId = statsResponse.connectionState?.kadId,
                    kadRunning = statsResponse.connectionState?.kadRunning,
                    serverDescription = statsResponse.connectionState?.serverDescription,
                    serverFailed = statsResponse.connectionState?.serverFailed,
                    serverFiles = statsResponse.connectionState?.serverFiles,
                    serverIpv4 = statsResponse.connectionState?.serverIpv4?.address,
                    serverPing = statsResponse.connectionState?.serverPing,
                    serverPrio = statsResponse.connectionState?.serverPrio,
                    serverStatic = statsResponse.connectionState?.serverStatic,
                    serverUsers = statsResponse.connectionState?.serverUsers,
                    serverUsersMax = statsResponse.connectionState?.serverUsersMax,
                    serverVersion = statsResponse.connectionState?.serverVersion
                ),
                downloadOverhead = statsResponse.downloadOverhead,
                downloadSpeed = statsResponse.downloadSpeed,
                downloadSpeedLimit = statsResponse.downloadSpeedLimit,
                ed2kFiles = statsResponse.ed2kFiles,
                ed2kUsers = statsResponse.ed2kUsers,
                kadFiles = statsResponse.kadFiles,
                kadFirewalledUdp = statsResponse.kadFirewalledUdp,
                kadIndexedKeywords = statsResponse.kadIndexedKeywords,
                kadIndexedLoad = statsResponse.kadIndexedLoad,
                kadIndexedNotes = statsResponse.kadIndexedNotes,
                kadIndexedSources = statsResponse.kadIndexedSources,
                kadIpAddress = statsResponse.kadIpAddress,
                kadIsRunningInLanMode = statsResponse.kadIsRunningInLanMode,
                kadNodes = statsResponse.kadNodes,
                kadUsers = statsResponse.kadUsers,
                loggerMessage = statsResponse.loggerMessage,
                sharedFileCount = statsResponse.sharedFileCount,
                totalReceivedBytes = statsResponse.totalReceivedBytes,
                totalSentBytes = statsResponse.totalSentBytes,
                totalSourceCount = statsResponse.totalSourceCount,
                uploadOverhead = statsResponse.uploadOverhead,
                uploadQueueLength = statsResponse.uploadQueueLength,
                uploadSpeed = statsResponse.uploadSpeed,
                uploadSpeedLimit = statsResponse.uploadSpeedLimit
            )
    }
}

@Serializable
data class ConnectionStatus(
    val clientId: Int? = null,
    val ed2kConnected: Boolean? = null,
    val ed2kConnecting: Boolean? = null,
    val ed2kId: Int? = null,
    val kadConnected: Boolean? = null,
    val kadFirewalled: Boolean? = null,
    val kadId: Int? = null,
    val kadRunning: Boolean? = null,
    val serverDescription: String? = null,
    val serverFailed: Int? = null,
    val serverFiles: Int? = null,
    val serverIpv4: String? = null,
    val serverPing: Int? = null,
    val serverPrio: Int? = null,
    val serverStatic: Boolean? = null,
    val serverUsers: Int? = null,
    val serverUsersMax: Int? = null,
    val serverVersion: String? = null
)