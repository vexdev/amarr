package amarr

import amarr.amule.debugApi
import amarr.category.CategoryStore
import amarr.torrent.torrentApi
import amarr.torznab.indexer.AmuleIndexer
import amarr.torznab.torznabApi
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.util.logging.*
import jamule.AmuleClient
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.VisibleForTesting
import org.slf4j.event.Level

lateinit var AMULE_PORT: String
lateinit var AMULE_HOST: String
lateinit var AMULE_PASSWORD: String
lateinit var AMARR_CONFIG_PATH: String
lateinit var AMULE_FINISHED_PATH: String
lateinit var AMARR_LOG_LEVEL: String

fun main() {
    loadEnv()
    embeddedServer(
        Netty, port = 8080
    ) {
        app()
    }.start(wait = true)
}

@VisibleForTesting
internal fun Application.app() {
    setLogLevel(log)
    val amuleClient = buildClient(log)
    val amuleIndexer = AmuleIndexer(amuleClient, log)
    val categoryStore = CategoryStore(AMARR_CONFIG_PATH)

    install(CallLogging) {
        level = Level.DEBUG
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
            encodeDefaults = true
        })
    }
    debugApi(amuleClient)
    torznabApi(amuleIndexer)
    torrentApi(amuleClient, categoryStore)
}

private fun setLogLevel(logger: Logger) {
    val logBackLogger = logger as ch.qos.logback.classic.Logger
    when (AMARR_LOG_LEVEL) {
        "DEBUG" -> logBackLogger.level = ch.qos.logback.classic.Level.DEBUG
        "INFO" -> logBackLogger.level = ch.qos.logback.classic.Level.INFO
        "WARN" -> logBackLogger.level = ch.qos.logback.classic.Level.WARN
        "ERROR" -> logBackLogger.level = ch.qos.logback.classic.Level.ERROR
        else -> throw Exception("Unknown log level: $AMARR_LOG_LEVEL")
    }
}

private fun loadEnv() {
    AMULE_PORT = System.getenv("AMULE_PORT").apply {
        if (this == null) throw Exception("AMULE_PORT is not set")
    }
    AMULE_HOST = System.getenv("AMULE_HOST").apply {
        if (this == null) throw Exception("AMULE_HOST is not set")
    }
    AMULE_PASSWORD = System.getenv("AMULE_PASSWORD").apply {
        if (this == null) throw Exception("AMULE_PASSWORD is not set")
    }
    AMULE_FINISHED_PATH = System.getenv("AMULE_FINISHED_PATH").let { it ?: "/finished" }
    AMARR_CONFIG_PATH = System.getenv("AMARR_CONFIG_PATH").let { it ?: "/config" }
    AMARR_LOG_LEVEL = System.getenv("AMARR_LOG_LEVEL").let { it ?: "INFO" }
}

fun buildClient(logger: Logger): AmuleClient =
    AmuleClient(AMULE_HOST, AMULE_PORT.toInt(), AMULE_PASSWORD, logger = logger)

