package io.github.halilozel1903.spaceflightnews

import io.github.halilozel1903.spaceflightnews.model.byline
import io.github.halilozel1903.spaceflightnews.model.hasImage
import io.github.halilozel1903.spaceflightnews.model.publishedInstant
import io.github.halilozel1903.spaceflightnews.model.updatedInstant
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ClientTest {

    @Test
    fun decodesArticlesWithLaunches() = runTest {
        MockApi().use { api ->
            val page = api.client.articles {
                hasLaunch = true
                limit = 1
            }
            assertEquals(12302, page.count)
            assertTrue(page.hasNext)
            val article = page.results.single()
            assertEquals(40430, article.id)
            assertEquals("NASA", article.newsSite)
            assertEquals(listOf("0a5d89b0-aa14-4140-b72d-fbcc8340b222", "283c3310-50e9-49fb-992d-38b71e02c36f"), article.launches.map { it.launchId })
            assertEquals("Launch Library 2", article.launches.first().provider)
            assertTrue(article.events.isEmpty())
            assertFalse(article.featured)
            assertNull(article.authors.single().socials)
            assertEquals(Instant.parse("2026-10-19T04:05:00Z"), article.publishedInstant)
            assertEquals(Instant.parse("2026-10-19T09:41:34.270231Z"), article.updatedInstant)
        }
    }

    @Test
    fun decodesAuthorsWithSocialsAndEvents() = runTest {
        MockApi().use { api ->
            val page = api.client.articles {
                newsSites("SpaceNews")
                hasEvent = true
                limit = 2
            }
            assertEquals(listOf(38745, 38678), page.results.map { it.id })
            val first = page.results.first()
            assertEquals(listOf(1493), first.events.map { it.eventId })
            assertEquals(listOf(1309, 1492), page.results[1].events.map { it.eventId })
            val socials = first.authors.single().socials!!
            assertEquals("https://x.com/jeff_foust", socials.x)
            assertEquals("", socials.youtube)
            assertEquals(listOf("x", "linkedin", "mastodon", "bluesky"), socials.links.keys.toList())
            assertEquals("Jeff Foust", first.byline)
        }
    }

    @Test
    fun loadsOneArticle() = runTest {
        MockApi().use { api ->
            val article = api.client.article(38745)
            assertEquals("NASA awards nearly \$600 million in lunar lander missions", article.title)
            assertEquals("https://spacenews.com/nasa-awards-nearly-600-million-in-lunar-lander-missions/", article.url)
            assertTrue(article.hasImage)
            assertEquals(listOf("${SpaceflightNewsClient.DEFAULT_BASE_URL}articles/38745/"), api.urls)
        }
    }

    @Test
    fun toleratesNullsAndUnknownFields() = runTest {
        MockApi().use { api ->
            val article = api.client.article(1)
            assertEquals("", article.imageUrl)
            assertEquals("", article.summary)
            assertFalse(article.hasImage)
            assertNull(article.byline)
        }
    }

    @Test
    fun decodesBlogs() = runTest {
        MockApi().use { api ->
            val page = api.client.blogs { limit = 2 }
            assertEquals(2181, page.count)
            assertEquals("Spacewalk Preps and Advanced Biomedical Research Begin Week", page.results.first().title)
            assertEquals(listOf(2559, 2558), page.results.map { it.id })
        }
    }

    @Test
    fun decodesReports() = runTest {
        MockApi().use { api ->
            val page = api.client.reports {
                search = "starliner"
                limit = 2
            }
            assertEquals(4, page.count)
            val report = page.results.first()
            assertEquals("Starliner arrives safely back on Earth", report.title)
            assertEquals("Boeing", report.newsSite)
            assertTrue(report.authors.isEmpty())
        }
    }

    @Test
    fun loadsInfo() = runTest {
        MockApi().use { api ->
            val info = api.client.info()
            assertEquals("4.33.0", info.version)
            assertEquals(42, info.newsSites.size)
            assertTrue("NASASpaceflight" in info.newsSites)
            val sorted = api.client.newsSites()
            assertEquals("ABC News", sorted.first())
            assertEquals("Virgin Galactic", sorted.last())
        }
    }

    @Test
    fun sendsUserAgentAndAcceptsJson() = runTest {
        MockApi().use { api ->
            api.client.info()
            val headers = api.requests.single().headers
            assertTrue(headers[HttpHeaders.UserAgent]!!.startsWith("spaceflight-news-kotlin/"))
            assertEquals("application/json", headers[HttpHeaders.Accept])
        }
    }

    @Test
    fun mapsErrors() = runTest {
        MockApi().use { api ->
            assertFailsWith<SpaceflightNewsException.NotFound> { api.client.article(999_999_999) }
        }
        MockApi(status = HttpStatusCode.BadRequest, body = "{\"published_at_gte\":[\"Enter a valid date/time.\"]}").use { api ->
            val error = assertFailsWith<SpaceflightNewsException.Http> { api.client.articles() }
            assertEquals(400, error.status)
            assertTrue(error.body!!.contains("published_at_gte"))
        }
        MockApi(status = HttpStatusCode.ServiceUnavailable).use { api ->
            assertEquals(503, assertFailsWith<SpaceflightNewsException.Http> { api.client.info() }.status)
        }
        MockApi(body = "<html>maintenance</html>").use { api ->
            assertFailsWith<SpaceflightNewsException.Decoding> { api.client.info() }
        }
    }

    @Test
    fun wrapsTransportFailures() = runTest {
        val client = SpaceflightNewsClient { transport = SpaceflightNewsTransportThatFails }
        val error = assertFailsWith<SpaceflightNewsException.Network> { client.info() }
        assertEquals("${SpaceflightNewsClient.DEFAULT_BASE_URL}info/", error.url)
    }

    @Test
    fun rejectsInvalidIds() = runTest {
        MockApi().use { api ->
            assertFailsWith<IllegalArgumentException> { api.client.article(0) }
            assertTrue(api.requests.isEmpty())
        }
    }
}

private val SpaceflightNewsTransportThatFails = io.github.halilozel1903.spaceflightnews.transport.SpaceflightNewsTransport {
    throw java.io.IOException("offline")
}
