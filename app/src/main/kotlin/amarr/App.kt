package amarr

import amarr.amule.debugApi
import amarr.torznab.torznabApi
import com.iukonline.amule.ec.v204.ECClientV204
import io.ktor.serialization.kotlinx.xml.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
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
    val client = buildClient()
    val socket = Socket(AMULE_HOST, AMULE_PORT.toInt())
    socket.use {
        client.setSocket(socket)
        embeddedServer(
            Netty, port = 8080
        ) {
            app(client)
        }.start(wait = true)
    }
}

private fun Application.app(client: ECClientV204) {
    install(CallLogging) {
        level = Level.INFO
    }
    install(ContentNegotiation) {
        xml()
    }
    debugApi(client)
    torznabApi(client)
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

fun buildClient(): ECClientV204 {
    val client = ECClientV204()
    client.setClientName("amarr")
    client.setClientVersion("SNAPSHOT")
    client.setPassword(AMULE_PASSWORD)
    return client
}
