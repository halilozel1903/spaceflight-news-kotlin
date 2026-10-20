package io.github.halilozel1903.spaceflightnews

import io.github.halilozel1903.spaceflightnews.model.Article
import io.github.halilozel1903.spaceflightnews.model.Info
import io.github.halilozel1903.spaceflightnews.model.NewsItem
import io.github.halilozel1903.spaceflightnews.model.Page
import io.github.halilozel1903.spaceflightnews.model.Report
import io.github.halilozel1903.spaceflightnews.transport.KtorTransport
import io.github.halilozel1903.spaceflightnews.transport.SpaceflightNewsTransport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * A coroutine client for the [Spaceflight News API v4](https://api.spaceflightnewsapi.net/v4/docs/) by The Space
 * Devs: articles, blogs and reports from spaceflight news sites, linked to launches and events.
 *
 * ```kotlin
 * val client = SpaceflightNewsClient()
 * val latest = client.articles { limit = 20 }
 * val artemis = client.articles { search = "Artemis"; newsSites("NASA") }
 * val sites = client.info().newsSites
 * ```
 *
 * Every function is main-safe and throws a [SpaceflightNewsException] on failure. Close the client when you no
 * longer need it.
 */
public class SpaceflightNewsClient(configure: SpaceflightNewsConfig.() -> Unit = {}) : AutoCloseable {

    private val config = SpaceflightNewsConfig().apply(configure)

    /** The API root, always ending with a slash. */
    public val baseUrl: String = config.baseUrl.let { if (it.endsWith('/')) it else "$it/" }

    private val transport: SpaceflightNewsTransport = config.transport ?: KtorTransport(
        httpClient = config.httpClient,
        engine = config.engine,
        userAgent = config.userAgent,
        requestTimeoutMillis = config.requestTimeoutMillis,
        retries = config.retries,
    )

    // region Articles, blogs and reports

    /** `GET /articles/` with [query]. */
    public suspend fun articles(query: NewsQuery = NewsQuery()): Page<Article> = list(NewsEndpoint.Articles, query)

    /** `GET /articles/` with a query built in [block]. */
    public suspend fun articles(block: NewsQuery.Builder.() -> Unit): Page<Article> = articles(newsQuery(block))

    /** `GET /articles/{id}/`. */
    public suspend fun article(id: Int): Article = item(NewsEndpoint.Articles, id)

    /** `GET /blogs/` with [query]. */
    public suspend fun blogs(query: NewsQuery = NewsQuery()): Page<Article> = list(NewsEndpoint.Blogs, query)

    /** `GET /blogs/` with a query built in [block]. */
    public suspend fun blogs(block: NewsQuery.Builder.() -> Unit): Page<Article> = blogs(newsQuery(block))

    /** `GET /blogs/{id}/`. */
    public suspend fun blog(id: Int): Article = item(NewsEndpoint.Blogs, id)

    /** `GET /reports/` with [query]. Reports have no launches, events or featured flag, so those filters throw. */
    public suspend fun reports(query: NewsQuery = NewsQuery()): Page<Report> = list(NewsEndpoint.Reports, query)

    /** `GET /reports/` with a query built in [block]. */
    public suspend fun reports(block: NewsQuery.Builder.() -> Unit): Page<Report> = reports(newsQuery(block))

    /** `GET /reports/{id}/`. */
    public suspend fun report(id: Int): Report = item(NewsEndpoint.Reports, id)

    /** `GET /info/`: the API version and the news sites it collects from. */
    public suspend fun info(): Info = fetch("${baseUrl}info/", Info.serializer())

    /** The names of every news site, from [info], sorted case-insensitively. */
    public suspend fun newsSites(): List<String> = info().newsSites.sortedWith(String.CASE_INSENSITIVE_ORDER)

    // endregion

    // region Generic access and paging

    /** One page of [endpoint] with [query]. */
    public suspend fun <T : NewsItem> list(endpoint: NewsEndpoint<T>, query: NewsQuery = NewsQuery()): Page<T> =
        fetch(listUrl(endpoint, query), Page.serializer(endpoint.serializer))

    /** One item of [endpoint] by id. */
    public suspend fun <T : NewsItem> item(endpoint: NewsEndpoint<T>, id: Int): T {
        require(id > 0) { "ids start at 1, was $id" }
        return fetch("$baseUrl${endpoint.path}/$id/", endpoint.serializer)
    }

    /** The page after [page] (its `next` url), or `null` on the last page. */
    public suspend fun <T : NewsItem> nextPage(endpoint: NewsEndpoint<T>, page: Page<T>): Page<T>? =
        page.next?.let { fetch(it, Page.serializer(endpoint.serializer)) }

    /** The page before [page] (its `previous` url), or `null` on the first page. */
    public suspend fun <T : NewsItem> previousPage(endpoint: NewsEndpoint<T>, page: Page<T>): Page<T>? =
        page.previous?.let { fetch(it, Page.serializer(endpoint.serializer)) }

    /**
     * Every page of [endpoint] matching [query], requested lazily as the flow is collected: the first page from
     * [query], then each page's `next` url as the API returns it. Stop early with `take(n)` or `first { }`.
     */
    public fun <T : NewsItem> pages(endpoint: NewsEndpoint<T>, query: NewsQuery = NewsQuery()): Flow<Page<T>> = flow {
        var page: Page<T>? = list(endpoint, query)
        while (page != null) {
            emit(page)
            page = nextPage(endpoint, page)
        }
    }

    /** Every item of [endpoint] matching [query] one by one, loading pages as needed. */
    public fun <T : NewsItem> all(endpoint: NewsEndpoint<T>, query: NewsQuery = NewsQuery()): Flow<T> = flow {
        pages(endpoint, query).collect { page -> page.results.forEach { emit(it) } }
    }

    /** Loads and decodes any url of the API with [deserializer]. */
    public suspend fun <T> fetch(url: String, deserializer: DeserializationStrategy<T>): T {
        val body = try {
            transport.get(url)
        } catch (e: SpaceflightNewsException) {
            throw e
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Exception) {
            throw SpaceflightNewsException.Network(url, e)
        }
        return try {
            json.decodeFromString(deserializer, body)
        } catch (e: SerializationException) {
            throw SpaceflightNewsException.Decoding(url, e)
        } catch (e: IllegalArgumentException) {
            throw SpaceflightNewsException.Decoding(url, e)
        }
    }

    /** The url of one page of [endpoint] with [query]. */
    public fun listUrl(endpoint: NewsEndpoint<*>, query: NewsQuery = NewsQuery()): String {
        require(endpoint.supportsLinks || !query.usesLinkFilters) {
            "/${endpoint.path} does not support has_launch, has_event, launch, event or is_featured"
        }
        val queryString = query.toQueryString()
        return "$baseUrl${endpoint.path}/" + if (queryString.isEmpty()) "" else "?$queryString"
    }

    // endregion

    /** Releases the HTTP client this instance created. */
    override fun close() {
        (transport as? AutoCloseable)?.close()
    }

    public companion object {
        /** `https://api.spaceflightnewsapi.net/v4/`. */
        public const val DEFAULT_BASE_URL: String = "https://api.spaceflightnewsapi.net/v4/"

        /** The page size the API uses when no `limit` is given. */
        public const val DEFAULT_PAGE_SIZE: Int = 10

        internal val json: Json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
        }
    }
}
