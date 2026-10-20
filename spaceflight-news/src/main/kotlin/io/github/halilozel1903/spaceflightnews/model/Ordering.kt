package io.github.halilozel1903.spaceflightnews.model

/** Values of the `ordering` parameter. The API's default is [PublishedAtDescending]. */
public enum class Ordering(public val apiValue: String) {
    PublishedAtAscending("published_at"),
    PublishedAtDescending("-published_at"),
    UpdatedAtAscending("updated_at"),
    UpdatedAtDescending("-updated_at"),
}
