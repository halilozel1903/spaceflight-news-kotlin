package io.github.halilozel1903.spaceflightnews

import io.github.halilozel1903.spaceflightnews.model.Article
import io.github.halilozel1903.spaceflightnews.model.NewsItem
import io.github.halilozel1903.spaceflightnews.model.Report
import kotlinx.serialization.KSerializer

/**
 * One of the API's three lists: [Articles], [Blogs] or [Reports]. Pass it to the generic functions of
 * [SpaceflightNewsClient] (`list`, `item`, `pages`, `all`, `nextPage`).
 */
public class NewsEndpoint<T : NewsItem> private constructor(
    /** The path below the API root, such as `articles`. */
    public val path: String,
    internal val serializer: KSerializer<T>,
    /**
     * Whether the endpoint knows `has_launch`, `has_event`, `launch`, `event` and `is_featured`.
     * Reports have no launches, events or featured flag.
     */
    public val supportsLinks: Boolean,
) {
    override fun toString(): String = "NewsEndpoint($path)"

    public companion object {
        /** `/articles`: news articles. */
        public val Articles: NewsEndpoint<Article> = NewsEndpoint("articles", Article.serializer(), supportsLinks = true)

        /** `/blogs`: blog posts, in the article schema. */
        public val Blogs: NewsEndpoint<Article> = NewsEndpoint("blogs", Article.serializer(), supportsLinks = true)

        /** `/reports`: station and mission reports. */
        public val Reports: NewsEndpoint<Report> = NewsEndpoint("reports", Report.serializer(), supportsLinks = false)

        /** All three, in the API's order. */
        public val entries: List<NewsEndpoint<*>> get() = listOf(Articles, Blogs, Reports)
    }
}
