package io.github.halilozel1903.spaceflightnews

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Reads `src/test/resources/fixtures/<name>`. The fixtures follow the shape of real v4 responses (field names,
 * nesting, nulls, ids and urls); summaries are shortened.
 */
internal fun fixture(name: String): String =
    requireNotNull(MockApi::class.java.getResource("/fixtures/$name")) { "Missing fixture $name" }.readText()

private const val BASE = SpaceflightNewsClient.DEFAULT_BASE_URL

/** Which fixture answers which url. Anything else is a 404. */
internal val routes: Map<String, String> = mapOf(
    "${BASE}articles/?has_launch=true&limit=1" to "articles-has-launch.json",
    "${BASE}articles/?news_site=SpaceNews&has_event=true&limit=2" to "articles-spacenews-events.json",
    "${BASE}articles/38745/" to "article-38745.json",
    "${BASE}articles/1/" to "article-nulls.json",
    "${BASE}blogs/?limit=2" to "blogs.json",
    "${BASE}reports/?search=starliner&limit=2" to "reports-starliner-0.json",
    "${BASE}reports/?limit=2&offset=2&search=starliner" to "reports-starliner-2.json",
    "${BASE}reports/?limit=2&search=starliner" to "reports-starliner-0.json",
    "${BASE}info/" to "info.json",
)

/** A client on a Ktor [MockEngine] serving the fixtures. [status] overrides the response for every url. */
internal class MockApi(
    private val status: HttpStatusCode? = null,
    private val body: String? = null,
) : AutoCloseable {
    val requests: MutableList<HttpRequestData> = CopyOnWriteArrayList()

    val engine = MockEngine { request ->
        requests += request
        val json = headersOf(HttpHeaders.ContentType, "application/json")
        when {
            status != null -> respond(body ?: "{}", status, json)
            body != null -> respond(body, HttpStatusCode.OK, json)
            else -> {
                val name = routes[request.url.toString()]
                if (name == null) {
                    respond("{\"detail\":\"No Article matches the given query.\"}", HttpStatusCode.NotFound, json)
                } else {
                    respond(fixture(name), HttpStatusCode.OK, json)
                }
            }
        }
    }

    val client = SpaceflightNewsClient {
        engine = this@MockApi.engine
        retries = 0
    }

    val urls: List<String> get() = requests.map { it.url.toString() }

    override fun close() {
        client.close()
        engine.close()
    }
}
