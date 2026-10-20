package io.github.halilozel1903.spaceflightnews.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * What articles, blogs and reports have in common. Dates are kept as the API sends them (ISO 8601, UTC);
 * [publishedInstant] and [updatedInstant] parse them.
 */
public sealed interface NewsItem {
    /** The id, unique within its kind (articles, blogs and reports are numbered separately). */
    public val id: Int
    public val title: String
    public val authors: List<Author>

    /** The original page on the news site. */
    public val url: String

    /** The lead image, or an empty string when the source has none. */
    public val imageUrl: String

    /** The publisher, as listed by `GET /info` ("NASA", "SpaceNews", ...). */
    public val newsSite: String
    public val summary: String

    /** `published_at`, for example `2026-10-19T04:05:00Z`. */
    public val publishedAt: String

    /** `updated_at`, for example `2026-10-19T09:41:34.270231Z`. */
    public val updatedAt: String
}

/** [NewsItem.publishedAt] as an [Instant], or `null` when it cannot be parsed. */
public val NewsItem.publishedInstant: Instant? get() = parseInstant(publishedAt)

/** [NewsItem.updatedAt] as an [Instant], or `null` when it cannot be parsed. */
public val NewsItem.updatedInstant: Instant? get() = parseInstant(updatedAt)

/** Whether the item has a lead image. */
public val NewsItem.hasImage: Boolean get() = imageUrl.isNotBlank()

/** The author names joined for a byline ("Jeff Foust", "A, B and C"), or `null` without authors. */
public val NewsItem.byline: String?
    get() {
        val names = authors.map { it.name.trim() }.filter { it.isNotEmpty() }.distinct()
        return when (names.size) {
            0 -> null
            1 -> names[0]
            else -> names.dropLast(1).joinToString(", ") + " and " + names.last()
        }
    }

private fun parseInstant(value: String): Instant? = try {
    Instant.parse(value)
} catch (e: DateTimeParseException) {
    null
}

/**
 * A news article or a blog post (`/articles` and `/blogs` share this schema). [launches] and [events] link it to
 * [Launch Library 2](https://thespacedevs.com/llapi) launches and events it covers.
 */
@Serializable
public data class Article(
    override val id: Int,
    override val title: String,
    override val authors: List<Author> = emptyList(),
    override val url: String,
    @SerialName("image_url") override val imageUrl: String = "",
    @SerialName("news_site") override val newsSite: String,
    override val summary: String = "",
    @SerialName("published_at") override val publishedAt: String,
    @SerialName("updated_at") override val updatedAt: String,
    /** Marked as featured by the Spaceflight News API team. */
    public val featured: Boolean = false,
    public val launches: List<LaunchRef> = emptyList(),
    public val events: List<EventRef> = emptyList(),
) : NewsItem

/** Blog posts use the article schema. */
public typealias Blog = Article

/** A report from a space station or mission, such as a Starliner or ISS status update. */
@Serializable
public data class Report(
    override val id: Int,
    override val title: String,
    override val authors: List<Author> = emptyList(),
    override val url: String,
    @SerialName("image_url") override val imageUrl: String = "",
    @SerialName("news_site") override val newsSite: String,
    override val summary: String = "",
    @SerialName("published_at") override val publishedAt: String,
    @SerialName("updated_at") override val updatedAt: String,
) : NewsItem

/** An author of an item. [socials] is `null` for most organisations. */
@Serializable
public data class Author(
    public val name: String,
    public val socials: Socials? = null,
)

/** An author's profiles. The API sends an empty string for a network the author is not on. */
@Serializable
public data class Socials(
    public val x: String = "",
    public val youtube: String = "",
    public val instagram: String = "",
    public val linkedin: String = "",
    public val mastodon: String = "",
    public val bluesky: String = "",
) {
    /** The profiles that are set, by network name ("x", "linkedin", ...), in the API's order. */
    public val links: Map<String, String>
        get() = buildMap<String, String> {
            listOf("x" to x, "youtube" to youtube, "instagram" to instagram, "linkedin" to linkedin, "mastodon" to mastodon, "bluesky" to bluesky)
                .filter { it.second.isNotBlank() }
                .forEach { (network, link) -> put(network, link) }
        }
}

/** A launch an article covers: a Launch Library 2 launch UUID. */
@Serializable
public data class LaunchRef(
    @SerialName("launch_id") public val launchId: String,
    public val provider: String = "",
)

/** An event an article covers: a Launch Library 2 event id. */
@Serializable
public data class EventRef(
    @SerialName("event_id") public val eventId: Int,
    public val provider: String = "",
)

/**
 * One page of a list endpoint. [next] and [previous] are the API's own urls for the neighbouring pages, with the
 * same filters, or `null` at either end.
 */
@Serializable
public data class Page<T>(
    /** How many items match the query in total. */
    public val count: Int,
    public val next: String? = null,
    public val previous: String? = null,
    public val results: List<T> = emptyList(),
) {
    public val hasNext: Boolean get() = next != null
}

/** `GET /info`: the API version and every news site it collects from. */
@Serializable
public data class Info(
    public val version: String,
    @SerialName("news_sites") public val newsSites: List<String> = emptyList(),
)
