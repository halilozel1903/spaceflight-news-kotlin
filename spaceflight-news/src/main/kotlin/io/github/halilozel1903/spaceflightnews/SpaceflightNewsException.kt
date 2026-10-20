package io.github.halilozel1903.spaceflightnews

/** Every failure of [SpaceflightNewsClient]. Catch the subclasses you care about, or all of them. */
public sealed class SpaceflightNewsException(
    /** The requested url. */
    public val url: String,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {

    /** The API answered 404: there is no item with that id. */
    public class NotFound(url: String) : SpaceflightNewsException(url, "Not found: $url")

    /** The API answered with another unsuccessful [status] (400 for a bad filter, 429, 5xx, ...). */
    public class Http(url: String, public val status: Int, public val body: String? = null) :
        SpaceflightNewsException(url, "HTTP $status for $url" + (body?.takeIf { it.isNotBlank() }?.let { ": ${it.take(200)}" } ?: ""))

    /** The request failed before a response arrived (offline, timeout, DNS). */
    public class Network(url: String, cause: Throwable) :
        SpaceflightNewsException(url, "Request failed for $url: ${cause.message ?: cause::class.simpleName}", cause)

    /** The response was not the JSON this library expects. */
    public class Decoding(url: String, cause: Throwable) :
        SpaceflightNewsException(url, "Could not decode the response of $url: ${cause.message}", cause)
}
