package amarr.category

import amarr.torrent.model.Category

interface CategoryStore {
    fun store(category: String, hash: String)
    fun getCategory(hash: String): String?
    fun delete(hash: String)
    fun addCategory(category: Category)
    fun getCategories(): Set<Category>
}