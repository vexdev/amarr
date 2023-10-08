package amarr.amule

import com.iukonline.amule.ec.v204.ECClientV204
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.debugApi(client: ECClientV204) {
    routing {
        get("/status") {
            call.respondText(client.stats.toString())
        }
    }
}