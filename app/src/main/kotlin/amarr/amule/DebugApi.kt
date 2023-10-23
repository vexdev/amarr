package amarr.amule

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jamule.AmuleClient

fun Application.debugApi(client: AmuleClient) {
    routing {
        get("/status") {
            call.respond(client.getStats())
        }
    }
}