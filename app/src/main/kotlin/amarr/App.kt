package amarr

import amarr.amule.AmuleClient
import amarr.amule.debugApi
import amarr.torrent.torrentApi
import amarr.torznab.torznabApi
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.xml.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.util.logging.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

lateinit var AMULE_PORT: String
lateinit var AMULE_HOST: String
lateinit var AMULE_PASSWORD: String
lateinit var AMARR_URL: String
const val FINISHED_FOLDER = "/finished"

fun main() {
    loadEnv()
    buildClient(
        LoggerFactory.getLogger("AmuleClient")
    ).use { amuleClient ->
        amuleClient.connect()
        embeddedServer(
            Netty, port = 8080
        ) {
            app(amuleClient)
        }.start(wait = true)
    }
}

private fun Application.app(amuleClient: AmuleClient) {

    install(CallLogging) {
        level = Level.DEBUG
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

fun buildClient(logger: Logger): AmuleClient = AmuleClient(AMULE_HOST, AMULE_PORT.toInt(), AMULE_PASSWORD, logger)

