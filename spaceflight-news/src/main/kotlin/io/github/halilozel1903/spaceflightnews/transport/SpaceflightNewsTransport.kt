package io.github.halilozel1903.spaceflightnews.transport

/**
 * Fetches the body of an API url. The client uses a Ktor based transport by default; supply your own through
 * `SpaceflightNewsConfig.transport` to serve responses from files, a database or another HTTP stack.
 *
 * Throw [io.github.halilozel1903.spaceflightnews.SpaceflightNewsException.NotFound] for missing items; other
 * exceptions are reported as [io.github.halilozel1903.spaceflightnews.SpaceflightNewsException.Network].
 */
public fun interface SpaceflightNewsTransport {
    public suspend fun get(url: String): String
}
