package io.github.halilozel1903.spaceflightnews

import io.github.halilozel1903.spaceflightnews.model.Ordering
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class QueryTest {
    private val client = SpaceflightNewsClient { transport = io.github.halilozel1903.spaceflightnews.transport.SpaceflightNewsTransport { error("unused") } }
    private val base = SpaceflightNewsClient.DEFAULT_BASE_URL

    @Test
    fun emptyQueryHasNoParameters() {
        assertEquals("${base}articles/", client.listUrl(NewsEndpoint.Articles))
        assertEquals("${base}reports/", client.listUrl(NewsEndpoint.Reports, NewsQuery()))
    }

    @Test
    fun mapsEveryParameter() {
        val query = newsQuery {
            search = "Artemis II"
            titleContains = "Orion"
            summaryContains = "heat shield"
            newsSites("NASA", "Spaceflight Now")
            excludedNewsSites = listOf("SpaceX")
            publishedAfter = Instant.parse("2026-01-01T00:00:00Z")
            publishedBefore = Instant.parse("2026-06-30T23:59:59Z")
            updatedAfter = Instant.parse("2026-02-01T00:00:00Z")
            updatedBefore = Instant.parse("2026-07-01T00:00:00Z")
            hasLaunch = true
            hasEvent = false
            launchIds = listOf("0a5d89b0-aa14-4140-b72d-fbcc8340b222", "283c3310-50e9-49fb-992d-38b71e02c36f")
            eventIds = listOf(1309, 1492)
            isFeatured = false
            orderBy(Ordering.PublishedAtAscending, Ordering.UpdatedAtDescending)
            limit = 25
            offset = 50
        }
        assertEquals(
            listOf(
                "search" to "Artemis II",
                "title_contains" to "Orion",
                "summary_contains" to "heat shield",
                "news_site" to "NASA,Spaceflight Now",
                "news_site_exclude" to "SpaceX",
                "published_at_gte" to "2026-01-01T00:00:00Z",
                "published_at_lte" to "2026-06-30T23:59:59Z",
                "updated_at_gte" to "2026-02-01T00:00:00Z",
                "updated_at_lte" to "2026-07-01T00:00:00Z",
                "has_launch" to "true",
                "has_event" to "false",
                "launch" to "0a5d89b0-aa14-4140-b72d-fbcc8340b222,283c3310-50e9-49fb-992d-38b71e02c36f",
                "event" to "1309,1492",
                "is_featured" to "false",
                "ordering" to "published_at,-updated_at",
                "limit" to "25",
                "offset" to "50",
            ),
            query.parameters(),
        )
        assertEquals(
            "${base}articles/?search=Artemis%20II&title_contains=Orion&summary_contains=heat%20shield" +
                "&news_site=NASA,Spaceflight%20Now&news_site_exclude=SpaceX" +
                "&published_at_gte=2026-01-01T00%3A00%3A00Z&published_at_lte=2026-06-30T23%3A59%3A59Z" +
                "&updated_at_gte=2026-02-01T00%3A00%3A00Z&updated_at_lte=2026-07-01T00%3A00%3A00Z" +
                "&has_launch=true&has_event=false" +
                "&launch=0a5d89b0-aa14-4140-b72d-fbcc8340b222,283c3310-50e9-49fb-992d-38b71e02c36f" +
                "&event=1309,1492&is_featured=false&ordering=published_at,-updated_at&limit=25&offset=50",
            client.listUrl(NewsEndpoint.Articles, query),
        )
    }

    @Test
    fun skipsBlankValuesAndEncodesSpecialCharacters() {
        val query = NewsQuery(search = "  ", titleContains = "Ariane 6 & Vega-C", newsSites = emptyList())
        assertEquals("title_contains=Ariane%206%20%26%20Vega-C", query.toQueryString())
    }

    @Test
    fun reportsRejectLinkFilters() {
        assertFailsWith<IllegalArgumentException> { client.listUrl(NewsEndpoint.Reports, NewsQuery(hasLaunch = true)) }
        assertFailsWith<IllegalArgumentException> { client.listUrl(NewsEndpoint.Reports, NewsQuery(eventIds = listOf(1))) }
        assertEquals("${base}blogs/?has_event=true", client.listUrl(NewsEndpoint.Blogs, NewsQuery(hasEvent = true)))
    }

    @Test
    fun validatesPaging() {
        assertFailsWith<IllegalArgumentException> { NewsQuery(limit = 0) }
        assertFailsWith<IllegalArgumentException> { NewsQuery(offset = -1) }
    }

    @Test
    fun customBaseUrlGetsATrailingSlash() {
        val custom = SpaceflightNewsClient { baseUrl = "https://example.org/v4"; transport = io.github.halilozel1903.spaceflightnews.transport.SpaceflightNewsTransport { "" } }
        assertEquals("https://example.org/v4/", custom.baseUrl)
        assertEquals("https://example.org/v4/articles/?limit=5", custom.listUrl(NewsEndpoint.Articles, NewsQuery(limit = 5)))
    }
}
