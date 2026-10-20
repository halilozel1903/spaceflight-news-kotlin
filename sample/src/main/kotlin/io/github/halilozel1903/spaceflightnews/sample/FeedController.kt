package io.github.halilozel1903.spaceflightnews.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.halilozel1903.spaceflightnews.model.NewsItem

/**
 * One feed (a list, a site filter and a search): the first page, then more pages as the list scrolls, each loaded
 * from the previous page's `next` url.
 */
class FeedController(
    private val repository: NewsRepository,
    val kind: NewsKind,
    val site: String?,
    val search: String,
) {
    /** The first page. */
    var first: Load<FeedPage> by mutableStateOf(Load.Loading)
        private set

    /** Everything loaded so far, in order. */
    val items = mutableStateListOf<NewsItem>()

    var loadingMore: Boolean by mutableStateOf(false)
        private set

    var moreFailed: Boolean by mutableStateOf(false)
        private set

    private var last: FeedPage? = null

    val hasMore: Boolean get() = last?.page?.next != null

    suspend fun start() {
        first = Load.Loading
        val result = load { repository.loadFeed(kind, site, search) }
        if (result is Load.Ready) {
            last = result.value
            items.clear()
            items.addAll(result.value.items)
        }
        first = result
    }

    suspend fun loadMore() {
        val page = last ?: return
        if (loadingMore || moreFailed || page.page.next == null) return
        loadingMore = true
        when (val result = load { repository.loadMore(page) }) {
            is Load.Ready -> {
                val next = result.value
                if (next != null) {
                    last = next
                    // Items published while paging shift the offsets; skip anything already shown.
                    val shown = items.mapTo(HashSet()) { it.id }
                    items.addAll(next.items.filter { it.id !in shown })
                }
            }
            is Load.Failed -> moreFailed = true
            Load.Loading -> Unit
        }
        loadingMore = false
    }

    fun retryMore() {
        moreFailed = false
    }
}
