package io.github.halilozel1903.spaceflightnews.sample

import io.github.halilozel1903.spaceflightnews.NewsEndpoint
import io.github.halilozel1903.spaceflightnews.NewsQuery
import io.github.halilozel1903.spaceflightnews.SpaceflightNewsClient
import io.github.halilozel1903.spaceflightnews.model.NewsItem
import io.github.halilozel1903.spaceflightnews.model.Page
import io.ktor.client.engine.okhttp.OkHttp

/** The three lists of the API, as the reader's segmented control shows them. */
enum class NewsKind(val label: String, val endpoint: NewsEndpoint<out NewsItem>) {
    Articles("Articles", NewsEndpoint.Articles),
    Blogs("Blogs", NewsEndpoint.Blogs),
    Reports("Reports", NewsEndpoint.Reports),
}

/** A loaded page of a feed: what to show and how to get more. */
data class FeedPage(val kind: NewsKind, val page: Page<out NewsItem>) {
    val items: List<NewsItem> get() = page.results
}

/**
 * The sample's only data source: the live Spaceflight News API, through [SpaceflightNewsClient] and its retries.
 * Paging follows each page's `next` url.
 */
class NewsRepository(private val client: SpaceflightNewsClient) {

    /** The news sites for the filter chips: well-known sites first, then the rest alphabetically. */
    suspend fun loadSites(): List<String> {
        val sites = client.newsSites()
        val first = FEATURED_SITES.filter { it in sites }
        return first + sites.filterNot { it in first }
    }

    /** The first page of [kind], newest first, optionally narrowed to one [site] and a [search]. */
    suspend fun loadFeed(kind: NewsKind, site: String?, search: String): FeedPage {
        val query = NewsQuery(
            search = search.ifBlank { null },
            newsSites = listOfNotNull(site),
            limit = PAGE_SIZE,
        )
        return FeedPage(kind, client.list(kind.endpoint, query))
    }

    /** The page after [feed], or `null` at the end. */
    suspend fun loadMore(feed: FeedPage): FeedPage? = nextOf(feed.kind.endpoint, feed.page)?.let { FeedPage(feed.kind, it) }

    private suspend fun <T : NewsItem> nextOf(endpoint: NewsEndpoint<T>, page: Page<out NewsItem>): Page<T>? {
        @Suppress("UNCHECKED_CAST")
        return client.nextPage(endpoint, page as Page<T>)
    }

    companion object {
        const val PAGE_SIZE = 20
        val FEATURED_SITES = listOf(
            "NASA",
            "SpaceNews",
            "Spaceflight Now",
            "NASASpaceflight",
            "ESA",
            "Space.com",
            "European Spaceflight",
            "SpacePolicyOnline.com",
            "Arstechnica",
            "Teslarati",
        )
    }
}

/** The app's repositories, created on first use. */
object Repositories {
    private var live: NewsRepository? = null
    private var scenes: NewsRepository? = null

    private fun client(retries: Int) = SpaceflightNewsClient {
        // Shares OkHttp with Coil.
        engine = OkHttp.create()
        userAgent = "spaceflight-news-kotlin-sample/1.0.0 (+https://github.com/halilozel1903/spaceflight-news-kotlin)"
        this.retries = retries
    }

    @Synchronized
    fun live(): NewsRepository = live ?: NewsRepository(client(retries = 2)).also { live = it }

    /** Screenshot scenes: more retries for a busy CI network. */
    @Synchronized
    fun scenes(): NewsRepository = scenes ?: NewsRepository(client(retries = 4)).also { scenes = it }
}
