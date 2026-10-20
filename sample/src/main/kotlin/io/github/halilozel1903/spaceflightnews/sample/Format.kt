package io.github.halilozel1903.spaceflightnews.sample

import io.github.halilozel1903.spaceflightnews.model.NewsItem
import io.github.halilozel1903.spaceflightnews.model.publishedInstant
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortDate = DateTimeFormatter.ofPattern("MMM d", Locale.US).withZone(ZoneOffset.UTC)
private val shortDateYear = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US).withZone(ZoneOffset.UTC)
private val longDate = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm 'UTC'", Locale.US).withZone(ZoneOffset.UTC)

/** "Just now", "12 min ago", "5 h ago", "3 d ago", then "Jun 30" or "Sep 7, 2024". */
fun NewsItem.ago(now: Instant = Instant.now()): String {
    val published = publishedInstant ?: return ""
    val elapsed = Duration.between(published, now)
    return when {
        elapsed.isNegative || elapsed.toMinutes() < 1 -> "Just now"
        elapsed.toMinutes() < 60 -> "${elapsed.toMinutes()} min ago"
        elapsed.toHours() < 24 -> "${elapsed.toHours()} h ago"
        elapsed.toDays() < 7 -> "${elapsed.toDays()} d ago"
        published.atZone(ZoneOffset.UTC).year == now.atZone(ZoneOffset.UTC).year -> shortDate.format(published)
        else -> shortDateYear.format(published)
    }
}

/** "Oct 19, 2026 at 04:05 UTC". */
fun NewsItem.publishedLong(): String = publishedInstant?.let { longDate.format(it) } ?: publishedAt

/** 1415 -> "1,415". */
fun Int.grouped(): String = String.format(Locale.US, "%,d", this)

/** "1 launch", "2 launches". */
fun plural(count: Int, one: String, many: String): String = if (count == 1) "1 $one" else "$count $many"
