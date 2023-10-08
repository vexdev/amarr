package amarr.torznab.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("caps")
data class Caps(
    val server: Server = Server(),
    val limits: Limits = Limits(),
    val searching: Searching = Searching(),
    val categories: Categories = Categories()
) {

    @Serializable
    @SerialName("server")
    data class Server(val version: String = "1.0", val title: String = "Amarr")

    @Serializable
    @SerialName("limits")
    data class Limits(val max: Int = 10000, val default: Int = 10000)

    @Serializable
    @SerialName("searching")
    data class Searching(
        val search: Search = Search(),
        val tvSearch: TvSearch = TvSearch(),
        val movieSearch: MovieSearch = MovieSearch(),
        val audioSearch: AudioSearch = AudioSearch(),
        val bookSearch: BookSearch = BookSearch()
    ) {
        @Serializable
        @SerialName("search")
        data class Search(
            val available: String = "yes",
            val supportedParams: String = "q"
        )

        @Serializable
        @SerialName("tv-search")
        data class TvSearch(
            val available: String = "no",
            val supportedParams: String = "q"
        )

        @Serializable
        @SerialName("movie-search")
        data class MovieSearch(
            val available: String = "no",
            val supportedParams: String = "q"
        )

        @Serializable
        @SerialName("audio-search")
        data class AudioSearch(
            val available: String = "no",
            val supportedParams: String = "q"
        )

        @Serializable
        @SerialName("book-search")
        data class BookSearch(
            val available: String = "no",
            val supportedParams: String = "q"
        )
    }

    @Serializable
    @SerialName("categories")
    class Categories(
        val category: List<Category> = listOf(
            Category(
                id = 1,
                name = "All",
            )
        )
    ) {
        @Serializable
        @SerialName("category")
        data class Category(
            val id: Int,
            val name: String,
        )
    }

}