package io.github.halilozel1903.spaceflightnews.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.spaceflightnews.model.Article
import io.github.halilozel1903.spaceflightnews.model.NewsItem
import io.github.halilozel1903.spaceflightnews.model.byline

/**
 * One item: its image, title, byline, date, summary, the launches and events it is linked to, and a button that
 * opens the full story on the news site. [onBack] is `null` on tablets, where the feed stays beside it.
 */
@Composable
fun DetailPane(item: NewsItem, onBack: (() -> Unit)?) {
    val uriHandler = LocalUriHandler.current
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
    ) {
        if (onBack != null) {
            Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    BackIcon(MaterialTheme.colorScheme.onSurface, Modifier.size(22.dp))
                }
                Text(item.newsSite, style = MaterialTheme.typography.titleMedium)
            }
        }
        Column(
            Modifier
                .padding(horizontal = 24.dp, vertical = if (onBack == null) 24.dp else 8.dp)
                .widthIn(max = 760.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NewsImage(
                item.imageUrl,
                item.newsSite,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (onBack == null) 2f else 16f / 9f)
                    .clip(RoundedCornerShape(24.dp)),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag(item.newsSite, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                if (item is Article) {
                    if (item.featured) Tag("Featured", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
                    if (item.launches.isNotEmpty()) {
                        Tag(plural(item.launches.size, "launch", "launches"), MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    if (item.events.isNotEmpty()) {
                        Tag(plural(item.events.size, "event", "events"), MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            Text(item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                listOfNotNull(item.byline?.let { "By $it" }, item.publishedLong()).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // The way to the full story stays in view above the summary, however long the summary is.
            Button(onClick = { uriHandler.openUri(item.url) }) {
                Text("Open on ${item.newsSite}")
                Spacer(Modifier.size(8.dp))
                ArrowOutIcon(MaterialTheme.colorScheme.onPrimary, Modifier.size(16.dp))
            }
            val summary = item.readableSummary()
            if (summary.isNotBlank()) {
                Text(summary, style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(
                    "${item.newsSite} did not include a summary.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Tag(text: String, container: Color, content: Color) {
    Surface(color = container, contentColor = content, shape = RoundedCornerShape(50)) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
fun EmptyDetail() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            "Pick a story to read its summary.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
