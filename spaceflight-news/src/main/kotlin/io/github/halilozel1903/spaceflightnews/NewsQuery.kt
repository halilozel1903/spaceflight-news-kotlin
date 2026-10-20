package io.github.halilozel1903.spaceflightnews

import io.github.halilozel1903.spaceflightnews.model.Ordering
import java.net.URLEncoder
import java.time.Instant

/**
 * Filters, ordering and paging of a list request. Every property maps to one query parameter of the API; `null`
 * and empty values are left out. Build one with [newsQuery] or pass the builder block to the client directly:
 *
 * ```kotlin
 * client.articles {
 *     search = "Artemis"
 *     newsSites = listOf("NASA", "SpaceNews")
 *     publishedAfter = Instant.parse("2026-01-01T00:00:00Z")
 *     hasLaunch = true
 *     limit = 20
 * }
 * ```
 */
public data class NewsQuery(
    /** `search`: words to find in the title or the summary. */
    public val search: String? = null,
    /** `title_contains`: text the title must contain. */
    public val titleContains: String? = null,
    /** `summary_contains`: text the summary must contain. */
    public val summaryContains: String? = null,
    /** `news_site`: only these sites (names as `GET /info` lists them). */
    public val newsSites: List<String> = emptyList(),
    /** `news_site_exclude`: leave these sites out. */
    public val excludedNewsSites: List<String> = emptyList(),
    /** `published_at_gte`: published at or after this instant. */
    public val publishedAfter: Instant? = null,
    /** `published_at_lte`: published at or before this instant. */
    public val publishedBefore: Instant? = null,
    /** `updated_at_gte`: updated at or after this instant. */
    public val updatedAfter: Instant? = null,
    /** `updated_at_lte`: updated at or before this instant. */
    public val updatedBefore: Instant? = null,
    /** `has_launch`: only items linked to at least one launch (`true`) or to none (`false`). Not for reports. */
    public val hasLaunch: Boolean? = null,
    /** `has_event`: only items linked to at least one event (`true`) or to none (`false`). Not for reports. */
    public val hasEvent: Boolean? = null,
    /** `launch`: items linked to any of these Launch Library 2 launch UUIDs. Not for reports. */
    public val launchIds: List<String> = emptyList(),
    /** `event`: items linked to any of these Launch Library 2 event ids. Not for reports. */
    public val eventIds: List<Int> = emptyList(),
    /** `is_featured`: only featured items (`true`) or only the others (`false`). Not for reports. */
    public val isFeatured: Boolean? = null,
    /** `ordering`: one or more sort keys. The API defaults to newest first. */
    public val ordering: List<Ordering> = emptyList(),
    /** `limit`: the page size. The API defaults to 10. */
    public val limit: Int? = null,
    /** `offset`: how many items to skip. */
    public val offset: Int? = null,
) {
    init {
        require(limit == null || limit > 0) { "limit must be positive, was $limit" }
        require(offset == null || offset >= 0) { "offset must not be negative, was $offset" }
    }

    /** Whether the query uses a filter that only articles and blogs know. */
    public val usesLinkFilters: Boolean
        get() = hasLaunch != null || hasEvent != null || launchIds.isNotEmpty() || eventIds.isNotEmpty() || isFeatured != null

    /** The query parameters in a fixed order, unencoded. */
    public fun parameters(): List<Pair<String, String>> = buildList<Pair<String, String>> {
        fun add(name: String, value: Any?) {
            when (value) {
                null -> Unit
                is String -> if (value.isNotBlank()) add(name to value.trim())
                is Collection<*> -> if (value.isNotEmpty()) add(name to value.joinToString(",") { it.toString().trim() })
                else -> add(name to value.toString())
            }
        }
        add("search", search)
        add("title_contains", titleContains)
        add("summary_contains", summaryContains)
        add("news_site", newsSites)
        add("news_site_exclude", excludedNewsSites)
        add("published_at_gte", publishedAfter)
        add("published_at_lte", publishedBefore)
        add("updated_at_gte", updatedAfter)
        add("updated_at_lte", updatedBefore)
        add("has_launch", hasLaunch)
        add("has_event", hasEvent)
        add("launch", launchIds)
        add("event", eventIds)
        add("is_featured", isFeatured)
        add("ordering", ordering.map { it.apiValue })
        add("limit", limit)
        add("offset", offset)
    }

    /** The encoded query string without the leading `?`, empty when nothing is set. */
    public fun toQueryString(): String = parameters().joinToString("&") { (name, value) ->
        "$name=${URLEncoder.encode(value, "UTF-8").replace("+", "%20").replace("%2C", ",")}"
    }

    /** A mutable builder for [NewsQuery]; see [newsQuery]. */
    public class Builder {
        public var search: String? = null
        public var titleContains: String? = null
        public var summaryContains: String? = null
        public var newsSites: List<String> = emptyList()
        public var excludedNewsSites: List<String> = emptyList()
        public var publishedAfter: Instant? = null
        public var publishedBefore: Instant? = null
        public var updatedAfter: Instant? = null
        public var updatedBefore: Instant? = null
        public var hasLaunch: Boolean? = null
        public var hasEvent: Boolean? = null
        public var launchIds: List<String> = emptyList()
        public var eventIds: List<Int> = emptyList()
        public var isFeatured: Boolean? = null
        public var ordering: List<Ordering> = emptyList()
        public var limit: Int? = null
        public var offset: Int? = null

        /** Shorthand for [newsSites]. */
        public fun newsSites(vararg sites: String) {
            newsSites = sites.toList()
        }

        /** Shorthand for [ordering]. */
        public fun orderBy(vararg keys: Ordering) {
            ordering = keys.toList()
        }

        public fun build(): NewsQuery = NewsQuery(
            search = search,
            titleContains = titleContains,
            summaryContains = summaryContains,
            newsSites = newsSites,
            excludedNewsSites = excludedNewsSites,
            publishedAfter = publishedAfter,
            publishedBefore = publishedBefore,
            updatedAfter = updatedAfter,
            updatedBefore = updatedBefore,
            hasLaunch = hasLaunch,
            hasEvent = hasEvent,
            launchIds = launchIds,
            eventIds = eventIds,
            isFeatured = isFeatured,
            ordering = ordering,
            limit = limit,
            offset = offset,
        )
    }
}

/** Builds a [NewsQuery]. */
public fun newsQuery(block: NewsQuery.Builder.() -> Unit): NewsQuery = NewsQuery.Builder().apply(block).build()
