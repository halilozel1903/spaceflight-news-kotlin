package io.github.halilozel1903.spaceflightnews

import io.github.halilozel1903.spaceflightnews.transport.SpaceflightNewsTransport
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine

/** Settings for [SpaceflightNewsClient], set in its builder block. */
public class SpaceflightNewsConfig internal constructor() {
    /** The API root. */
    public var baseUrl: String = SpaceflightNewsClient.DEFAULT_BASE_URL

    /**
     * The Ktor engine. `null` uses CIO. Pass `OkHttp.create()` (add `io.ktor:ktor-client-okhttp`) to share your
     * app's OkHttp stack, or a `MockEngine` in tests. The client does not close an engine you pass.
     */
    public var engine: HttpClientEngine? = null

    /** A ready Ktor client to use as is, instead of [engine]. [requestTimeoutMillis] and [retries] then do not apply. */
    public var httpClient: HttpClient? = null

    /** Replaces HTTP entirely, for example to serve recorded responses. */
    public var transport: SpaceflightNewsTransport? = null

    /** Sent with every request. */
    public var userAgent: String = "spaceflight-news-kotlin/1.0.0 (+https://github.com/halilozel1903/spaceflight-news-kotlin)"

    /** Timeout of one request, in milliseconds. */
    public var requestTimeoutMillis: Long = 30_000

    /** Retries on network errors, 429 and 5xx responses, with exponential backoff. */
    public var retries: Int = 2
}
