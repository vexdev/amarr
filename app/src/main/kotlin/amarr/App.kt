package amarr

import amarr.amule.debugApi
import amarr.category.FileCategoryStore
import amarr.torrent.torrentApi
import amarr.torznab.indexer.AmuleIndexer
import amarr.torznab.indexer.ddunlimitednet.DdunlimitednetClient
import amarr.torznab.indexer.ddunlimitednet.DdunlimitednetIndexer
import amarr.torznab.torznabApi
import io.ktor.client.engine.cio.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import jamule.AmuleClient
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.VisibleForTesting
import org.slf4j.Logger
import org.slf4j.event.Level

private val AMULE_PORT = System.getenv("AMULE_PORT").apply {
    if (this == null) throw Exception("AMULE_PORT is not set")
}
private val AMULE_HOST = System.getenv("AMULE_HOST").apply {
    if (this == null) throw Exception("AMULE_HOST is not set")
}
private val AMULE_PASSWORD = System.getenv("AMULE_PASSWORD").apply {
    if (this == null) throw Exception("AMULE_PASSWORD is not set")
}
private val AMULE_FINISHED_PATH = System.getenv("AMULE_FINISHED_PATH").let { it ?: "/finished" }
private val AMARR_CONFIG_PATH = System.getenv("AMARR_CONFIG_PATH").let { it ?: "/config" }
private val AMARR_LOG_LEVEL = System.getenv("AMARR_LOG_LEVEL").let { it ?: "INFO" }
private val DDUNLIMITEDNET_USERNAME = System.getenv("DDUNLIMITEDNET_USERNAME")
private val DDUNLIMITEDNET_PASSWORD = System.getenv("DDUNLIMITEDNET_PASSWORD")

fun main() {
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
    val ddunlimitednetClient = DdunlimitednetClient(CIO.create(), DDUNLIMITEDNET_USERNAME, DDUNLIMITEDNET_PASSWORD, log)
    val ddunlimitednetIndexer = DdunlimitednetIndexer(ddunlimitednetClient, log)
    val categoryStore = FileCategoryStore(AMARR_CONFIG_PATH)

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
    torznabApi(amuleIndexer, ddunlimitednetIndexer)
    torrentApi(amuleClient, categoryStore, AMULE_FINISHED_PATH)
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

fun buildClient(logger: Logger): AmuleClient =
    AmuleClient(AMULE_HOST, AMULE_PORT.toInt(), AMULE_PASSWORD, logger = logger)

