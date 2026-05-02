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
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import jamule.AmuleClient
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.VisibleForTesting
import org.slf4j.Logger
import org.slf4j.event.Level

fun main() {
    embeddedServer(
        Netty, port = amarrPort()
    ) {
        app()
    }.start(wait = true)
}

@VisibleForTesting
internal fun Application.app() {
    setLogLevel(log, optionalEnv("AMARR_LOG_LEVEL", "INFO"))
    val amuleClient = buildClient(log)
    val amuleIndexer = AmuleIndexer(amuleClient, log)
    val ddunlimitednetClient = DdunlimitednetClient(
        CIO.create(),
        System.getenv("DDUNLIMITEDNET_USERNAME"),
        System.getenv("DDUNLIMITEDNET_PASSWORD"),
        log
    )
    val ddunlimitednetIndexer = DdunlimitednetIndexer(ddunlimitednetClient, log)
    val categoryStore = FileCategoryStore(optionalEnv("AMARR_CONFIG_PATH", "/config"))

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
    torrentApi(amuleClient, categoryStore, optionalEnv("AMULE_FINISHED_PATH", "/finished"))
}

@VisibleForTesting
internal fun amarrPort(env: Map<String, String> = System.getenv()): Int {
    val port = optionalEnv(env, "AMARR_PORT", "8080").toIntOrNull()
        ?: throw Exception("AMARR_PORT must be a valid port number")
    if (port !in 1..65535) {
        throw Exception("AMARR_PORT must be between 1 and 65535")
    }
    return port
}

private fun setLogLevel(logger: Logger, logLevel: String) {
    val logBackLogger = logger as ch.qos.logback.classic.Logger
    when (logLevel) {
        "DEBUG" -> logBackLogger.level = ch.qos.logback.classic.Level.DEBUG
        "INFO" -> logBackLogger.level = ch.qos.logback.classic.Level.INFO
        "WARN" -> logBackLogger.level = ch.qos.logback.classic.Level.WARN
        "ERROR" -> logBackLogger.level = ch.qos.logback.classic.Level.ERROR
        else -> throw Exception("Unknown log level: $logLevel")
    }
}

fun buildClient(logger: Logger): AmuleClient =
    AmuleClient(
        requiredEnv("AMULE_HOST"),
        requiredEnv("AMULE_PORT").toInt(),
        requiredEnv("AMULE_PASSWORD"),
        logger = logger
    )

private fun requiredEnv(name: String): String = System.getenv(name) ?: throw Exception("$name is not set")

private fun optionalEnv(name: String, default: String): String = optionalEnv(System.getenv(), name, default)

private fun optionalEnv(env: Map<String, String>, name: String, default: String): String = env[name] ?: default
