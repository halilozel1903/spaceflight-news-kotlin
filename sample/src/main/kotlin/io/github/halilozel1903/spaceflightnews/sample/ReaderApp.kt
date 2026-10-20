package io.github.halilozel1903.spaceflightnews.sample

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.spaceflightnews.model.NewsItem
import kotlinx.coroutines.delay

/**
 * The whole reader: a feed on phones that opens a detail screen, and feed and detail side by side on wide screens,
 * all loaded from the live Spaceflight News API. Screenshot [scene]s start on a fixed list and search, and report to
 * a [SceneMonitor] when their data and images have loaded.
 */
@Composable
fun ReaderApp(scene: Scene?, repository: NewsRepository) {
    val monitor = remember(scene) { scene?.let { SceneMonitor(it) } }
    monitor?.Report()

    var kind by rememberSaveable { mutableStateOf(scene?.kind ?: NewsKind.Articles) }
    var site by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf(scene?.search ?: "") }
    // The search that was sent: the field's text after a short pause in typing, or right away on the search key.
    var search by rememberSaveable { mutableStateOf(scene?.search ?: "") }
    var reload by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<NewsItem?>(null) }

    LaunchedEffect(query) {
        delay(450)
        search = query.trim()
    }

    val sites by produceState<Load<List<String>>>(Load.Loading, repository, reload) {
        value = load { repository.loadSites() }
    }
    val feed = remember(repository, kind, site, search, reload) { FeedController(repository, kind, site, search) }
    LaunchedEffect(feed) { feed.start() }
    val listState = remember(feed) { LazyListState() }

    CompositionLocalProvider(LocalSceneMonitor provides monitor) {
        ReportLoad(sites)
        ReportLoad(feed.first)
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            val wide = maxWidth >= 840.dp
            val feedPane: @Composable (Modifier) -> Unit = { modifier ->
                FeedPane(
                    feed = feed,
                    sites = (sites as? Load.Ready)?.value.orEmpty(),
                    kind = kind,
                    onKindChange = {
                        kind = it
                        selected = null
                    },
                    site = site,
                    onSiteChange = {
                        site = it
                        selected = null
                    },
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { search = query.trim() },
                    selected = if (wide) selected ?: openByDefault(feed.items) else null,
                    onSelect = { selected = it },
                    onRetry = { reload++ },
                    listState = listState,
                    modifier = modifier,
                )
            }
            if (wide) {
                Row(Modifier.fillMaxSize()) {
                    feedPane(Modifier.weight(0.42f))
                    Box(
                        Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant),
                    )
                    Box(Modifier.weight(0.58f)) {
                        // On tablets a story is always open: the newest one worth reading until another one is picked.
                        val item = selected ?: openByDefault(feed.items)
                        when {
                            item != null -> DetailPane(item = item, onBack = null)
                            feed.first is Load.Ready -> EmptyDetail()
                            else -> Unit
                        }
                    }
                }
            } else {
                val item = selected
                if (item == null) {
                    feedPane(Modifier.fillMaxSize())
                } else {
                    BackHandler { selected = null }
                    DetailPane(item = item, onBack = { selected = null })
                }
            }
        }
    }
}

/**
 * The story a tablet opens before one is picked: the newest with an image and a real summary that fits the pane (some sources only
 * send a line like "From the ESA Blogs."), or simply the newest.
 */
fun openByDefault(items: List<NewsItem>): NewsItem? =
    items.firstOrNull { it.imageUrl.isNotBlank() && it.readableSummary().length in 160..520 } ?: items.firstOrNull()
