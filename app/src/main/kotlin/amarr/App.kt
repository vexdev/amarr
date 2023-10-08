package amarr

import amarr.amule.AmuleClient
import amarr.amule.debugApi
import amarr.torrent.torrentApi
import amarr.torznab.torznabApi
import amarr.tracker.trackerApi
import com.iukonline.amule.ec.v204.ECClientV204
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.xml.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.util.logging.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.net.Socket

lateinit var AMULE_PORT: String
lateinit var AMULE_HOST: String
lateinit var AMULE_PASSWORD: String
lateinit var AMARR_URL: String
const val REQUESTED_FOLDER = "/requested"
const val FINISHED_FOLDER = "/finished"

fun main() {
    loadEnv()
    embeddedServer(
        Netty, port = 8080
    ) {
        app()
    }.start(wait = true)
}

private fun Application.app() {
    val amuleClient = buildClient(log)
    install(CallLogging) {
        level = Level.INFO
    }
    install(ContentNegotiation) {
        xml()
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        })
    }
    debugApi(amuleClient)
    torznabApi(amuleClient)
    trackerApi(amuleClient)
    torrentApi(amuleClient)
}

fun loadEnv() {
    AMULE_PORT = System.getenv("AMULE_PORT").apply {
        if (this == null) throw Exception("AMULE_PORT is not set")
    }
    AMULE_HOST = System.getenv("AMULE_HOST").apply {
        if (this == null) throw Exception("AMULE_HOST is not set")
    }
    AMULE_PASSWORD = System.getenv("AMULE_PASSWORD").apply {
        if (this == null) throw Exception("AMULE_PASSWORD is not set")
    }
    AMARR_URL = System.getenv("AMARR_URL").apply {
        if (this == null) throw Exception("AMARR_URL is not set")
    }
}

fun buildClient(logger: Logger): AmuleClient {
    val client = ECClientV204()
    client.setClientName("amarr")
    client.setClientVersion("SNAPSHOT")
    client.setPassword(AMULE_PASSWORD)
    val socket = Socket(AMULE_HOST, AMULE_PORT.toInt())
    client.setSocket(socket)
    return AmuleClient(client, logger)
}
