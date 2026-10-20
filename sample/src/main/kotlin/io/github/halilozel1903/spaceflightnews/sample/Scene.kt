package io.github.halilozel1903.spaceflightnews.sample

/**
 * Screenshot scenes, picked with the `scene` intent extra. They load live data from the Spaceflight News API like a
 * normal launch, starting on a fixed [kind] and [search]. [loads] is how many loads (the news site list and the
 * first page of the feed) must finish before the scene is complete.
 */
enum class Scene(val key: String, val kind: NewsKind, val search: String) {
    Feed("feed", NewsKind.Articles, ""),
    Search("search", NewsKind.Articles, "Artemis"),
    Reports("reports", NewsKind.Reports, ""),
    Tablet("tablet", NewsKind.Articles, ""),
    ;

    val loads: Int get() = 2

    companion object {
        fun from(key: String?): Scene? = entries.firstOrNull { it.key == key }
    }
}
