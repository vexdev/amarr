package amarr.category

import amarr.torrent.model.Category
import java.io.File

/**
 * Stores the relation category - file hash in a file in the amule config directory.
 * The access to this file is synchronized.
 */
class FileCategoryStore(storePath: String) : CategoryStore {
    private val hashesCache: MutableMap<String, String> = mutableMapOf()
    private var categoriesCache: MutableSet<Category>? = null
    private var categoriesFilePath = File(storePath, CATEGORIES_FILE).absolutePath
    private var hashesFilePath = File(storePath, HASHES_FILE).absolutePath

    override fun store(category: String, hash: String) {
        synchronized(HASHES_FILE) {
            if (category.contains('\t') || hash.contains('\t'))
                throw IllegalArgumentException("Category or hash contains tab character")
            val file = File(hashesFilePath)
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            file.appendText("$hash\t$category\n")
            hashesCache[hash] = category
        }
    }

    override fun getCategory(hash: String): String? {
        synchronized(HASHES_FILE) {
            if (hashesCache.containsKey(hash))
                return hashesCache[hash]
            val file = File(hashesFilePath)
            if (!file.exists())
                return null
            val line = file.readLines().find { it.split('\t')[0] == hash } ?: return null
            val category = line.split('\t')[1]
            hashesCache[hash] = category
            return category
        }
    }

    override fun delete(hash: String) {
        synchronized(HASHES_FILE) {
            val file = File(hashesFilePath)
            if (!file.exists())
                return
            val lines = file.readLines()
            val line = lines.find { it.split('\t')[0] == hash } ?: return
            file.writeText(lines.filterNot { it == line }.joinToString("\n"))
            hashesCache.remove(hash)
        }
    }

    override fun addCategory(category: Category) {
        synchronized(CATEGORIES_FILE) {
            if (categoriesCache != null)
                categoriesCache!!.add(category)
            val file = File(categoriesFilePath)
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            file.appendText("${category.name}\t${category.savePath}\n")
        }
    }

    override fun getCategories(): Set<Category> {
        synchronized(CATEGORIES_FILE) {
            if (categoriesCache != null)
                return categoriesCache!!
            val file = File(categoriesFilePath)
            if (!file.exists())
                return emptySet()
            val categories = file.readLines().map { line ->
                val split = line.split('\t')
                Category(split[0], split[1])
            }
            categoriesCache = categories.toMutableSet()
            return categoriesCache!!
        }
    }

    companion object {
        private const val CATEGORIES_FILE = "categories.tsv"
        private const val HASHES_FILE = "hashes.tsv"
    }

}