package amarr.torrent

import amarr.amule.AmuleClient
import amarr.torrent.model.Category
import amarr.torrent.model.TorrentInfo

class TorrentService(private val amuleClient: AmuleClient) {
    private val categoryList = mutableListOf<Category>()

    fun getTorrentInfo(category: String?) = listOf(TorrentInfo(category = category))

    fun getCategories(): Map<String, Category> = categoryList.associateBy { it.name }

    fun addCategory(category: String) {
        categoryList.add(Category(category, ""))
    }

    fun addTorrent(urls: List<String>, category: String?, paused: String?) {
        TODO("Not yet implemented")
    }

}