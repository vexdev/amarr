package amarr.torznab.indexer

import amarr.torznab.model.Caps
import amarr.torznab.model.Feed

interface Indexer {

    /**
     * Given a paginated query, returns a [Feed] with the results.
     */
    fun search(query: String, offset: Int, limit: Int): Feed

    /**
     * Returns the capabilities of this indexer.
     */
    fun capabilities(): Caps

}