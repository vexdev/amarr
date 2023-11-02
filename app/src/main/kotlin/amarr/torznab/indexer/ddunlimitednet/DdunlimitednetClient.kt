package amarr.torznab.indexer.ddunlimitednet

import amarr.torznab.indexer.ThrottledException
import amarr.torznab.indexer.UnauthorizedException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.Logger
import java.util.regex.Pattern

class DdunlimitednetClient(
    engine: HttpClientEngine,
    private val username: String?,
    private val password: String?,
    private val log: Logger
) {
    private val ed2kPattern = Pattern.compile("ed2k://\\|file\\|.*?\\|/")
    private val httpClient = HttpClient(engine) {
        install(HttpCookies)
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    /**
     * Tries to search for a query, returning a list of ed2k links if successful.
     */
    suspend fun search(query: String): Result<List<String>> = httpClient.prepareForm(
        formParameters = Parameters.build {
            append(
                "keywords",
                query
            ) // TODO: This needs to be encoded somehow, like "this is us" becomes "this+is+us", obviously not url-encoded nor html-encoded, but maybe just a space replacement?
            append("terms", "all")
            append("gsearch", "0")
            append("author", "")
            append("sv", "0")
            append("fid%5B%5D", "1577")
            append("sc", "1")
            append("sf", "titleonly")
            append("sr", "posts")
            append("sk", "t")
            append("sd", "d")
            append("st", "0")
            append("ch", "-1")
            append("t", "0")
            append("submit", "Search")
        },
        url = "${URL_BASE}/search.php",
    ).execute { response ->
        val links = mutableListOf<String>()
        val body: String = response.body()
        // TODO: Implement pagination
        for (line in body.lineSequence()) {
            val lineWithoutTags = line.replace("<[^>]*>".toRegex(), "")
            if (lineWithoutTags.contains(NOT_LOGGED))
                return@execute Result.failure(UnauthorizedException())
            if (lineWithoutTags.contains(THROTTLED))
                return@execute Result.failure(ThrottledException())
            val ed2kMatcher = ed2kPattern.matcher(lineWithoutTags)
            while (ed2kMatcher.find()) {
                val ed2kLink = ed2kMatcher.group()
                links.add(StringEscapeUtils.unescapeHtml4(ed2kLink))
            }
        }
        Result.success(links)
    }

    suspend fun login() {
        if (username == null || password == null) {
            throw Exception("Username or password not set for ddunlimitednet indexer")
        }
        httpClient.submitForm(
            formParameters = Parameters.build {
                append("username", username)
                append("password", password)
                append("autologin", "on")
                append("redirect", "index.php")
                append("login", "Login")
            }, url = "${URL_BASE}/ucp.php", block = {
                parameters { append("mode", "login") }
            }
        )
    }

    companion object {
        private val NOT_LOGGED = "Non ti è permesso di utilizzare il sistema di ricerca"
        private val THROTTLED = "Sorry but you cannot use search at this time. Please try again in a few minutes."
        private val LOGGED_PATTERN = "Logout [ %s ]"
        private val URL_BASE = "https://ddunlimited.net"
        private val SID_COOKIE = "phpbb3_ddu4final_sid"
    }
}