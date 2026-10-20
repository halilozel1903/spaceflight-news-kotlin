package io.github.halilozel1903.spaceflightnews

import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PagingTest {
    private val base = SpaceflightNewsClient.DEFAULT_BASE_URL
    private val starliner = NewsQuery(search = "starliner", limit = 2)

    @Test
    fun followsTheNextUrlUntilTheLastPage() = runTest {
        MockApi().use { api ->
            val pages = api.client.pages(NewsEndpoint.Reports, starliner).toList()
            assertEquals(listOf(2, 2), pages.map { it.results.size })
            assertNull(pages.last().next)
            // The second request is the API's own `next` url, verbatim.
            assertEquals(
                listOf(
                    "${base}reports/?search=starliner&limit=2",
                    "${base}reports/?limit=2&offset=2&search=starliner",
                ),
                api.urls,
            )
        }
    }

    @Test
    fun allEmitsEveryItemLazily() = runTest {
        MockApi().use { api ->
            assertEquals(4, api.client.all(NewsEndpoint.Reports, starliner).count())
        }
        MockApi().use { api ->
            val first = api.client.all(NewsEndpoint.Reports, starliner).take(2).map { it.id }.toList()
            assertEquals(listOf(1662, 1661), first)
            assertEquals(1, api.requests.size)
        }
        MockApi().use { api ->
            assertEquals("Starliner Return to Earth Preps Underway", api.client.all(NewsEndpoint.Reports, starliner).first { it.id == 1656 }.title)
            assertEquals(2, api.requests.size)
        }
    }

    @Test
    fun nextAndPreviousPage() = runTest {
        MockApi().use { api ->
            val first = api.client.reports(starliner)
            val second = api.client.nextPage(NewsEndpoint.Reports, first)!!
            assertEquals(listOf(1660, 1656), second.results.map { it.id })
            assertNull(api.client.nextPage(NewsEndpoint.Reports, second))
            assertEquals(first, api.client.previousPage(NewsEndpoint.Reports, second))
            assertNull(api.client.previousPage(NewsEndpoint.Reports, first))
        }
    }
}
