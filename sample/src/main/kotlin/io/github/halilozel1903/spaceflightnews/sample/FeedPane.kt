package io.github.halilozel1903.spaceflightnews.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.spaceflightnews.model.NewsItem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * The feed: the app bar, a search field, the Articles / Blogs / Reports switch and the news site chips above the
 * list. Without a search the newest item is shown as a hero; more pages load as the list nears its end.
 */
@Composable
fun FeedPane(
    feed: FeedController,
    sites: List<String>,
    kind: NewsKind,
    onKindChange: (NewsKind) -> Unit,
    site: String?,
    onSiteChange: (String?) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    selected: NewsItem?,
    onSelect: (NewsItem) -> Unit,
    onRetry: () -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Header(query = query, onQueryChange = onQueryChange, onSearch = onSearch)
        SingleChoiceSegmentedButtonRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            NewsKind.entries.forEachIndexed { index, entry ->
                SegmentedButton(
                    selected = entry == kind,
                    onClick = { onKindChange(entry) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = NewsKind.entries.size),
                ) { Text(entry.label) }
            }
        }
        SiteChips(sites = sites, site = site, onSiteChange = onSiteChange)
        when (val state = feed.first) {
            Load.Loading -> LoadingState()
            is Load.Failed -> ErrorState(state.message, onRetry = onRetry)
            is Load.Ready -> FeedList(
                feed = feed,
                count = state.value.page.count,
                kind = kind,
                selected = selected,
                onSelect = onSelect,
                listState = listState,
            )
        }
    }
}

@Composable
private fun Header(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)) {
        Text(
            "Spaceflight News",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            placeholder = { Text("Search launches, missions, companies") },
            leadingIcon = { SearchIcon(MaterialTheme.colorScheme.onSurfaceVariant, Modifier.size(20.dp)) },
            trailingIcon = if (query.isEmpty()) {
                null
            } else {
                {
                    IconButton(onClick = { onQueryChange("") }) {
                        CloseIcon(MaterialTheme.colorScheme.onSurfaceVariant, Modifier.size(18.dp))
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        )
    }
}

@Composable
private fun SiteChips(sites: List<String>, site: String?, onSiteChange: (String?) -> Unit) {
    // The chosen site stays first-class even before the site list has loaded.
    val all = buildList<String> {
        if (site != null && site !in sites) add(site)
        addAll(sites)
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") {
            FilterChip(selected = site == null, onClick = { onSiteChange(null) }, label = { Text("All sites") })
        }
        items(all, key = { it }) { name ->
            FilterChip(
                selected = name == site,
                onClick = { onSiteChange(if (name == site) null else name) },
                label = { Text(name) },
            )
        }
    }
}

@Composable
private fun FeedList(
    feed: FeedController,
    count: Int,
    kind: NewsKind,
    selected: NewsItem?,
    onSelect: (NewsItem) -> Unit,
    listState: LazyListState,
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(feed, listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= info.totalItemsCount - 4 && !feed.moreFailed
        }.distinctUntilChanged().filter { it }.collect { feed.loadMore() }
    }
    val items = feed.items
    if (items.isEmpty()) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (feed.search.isNotEmpty()) "Nothing matches “${feed.search}”." else "Nothing here yet.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    // Without a search the newest item leads as the top story (with its site initial when it has no image).
    val showHero = feed.search.isEmpty()
    LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 24.dp)) {
        if (showHero) {
            item(key = "hero") {
                Hero(items.first(), selected = selected?.id == items.first().id, onClick = { onSelect(items.first()) })
            }
        }
        item(key = "label") {
            Text(
                if (feed.search.isNotEmpty()) {
                    "${count.grouped()} results for “${feed.search}”"
                } else {
                    "Latest ${kind.label.lowercase()}" + (feed.site?.let { " from $it" } ?: "")
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            )
        }
        val rest = if (showHero) items.drop(1) else items
        items(rest, key = { it.id }) { item ->
            StoryRow(item, selected = selected?.id == item.id, onClick = { onSelect(item) })
            HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        }
        item(key = "footer") {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    feed.loadingMore -> CircularProgressIndicator(Modifier.size(28.dp))
                    feed.moreFailed -> TextButton(onClick = {
                        feed.retryMore()
                        scope.launch { feed.loadMore() }
                    }) { Text("Could not load more. Try again") }
                    !feed.hasMore -> Text(
                        "You're all caught up",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** The newest item: a large image with the title over a dark gradient. */
@Composable
private fun Hero(item: NewsItem, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
    ) {
        NewsImage(item.imageUrl, item.newsSite, Modifier.fillMaxSize())
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0.35f to Color.Transparent, 1f to Color(0xE6070A14))),
        )
        if (selected) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Top story",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
            Text(
                item.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${item.newsSite} · ${item.ago()}",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun StoryRow(item: NewsItem, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "${item.newsSite} · ${item.ago()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        NewsImage(
            item.imageUrl,
            item.newsSite,
            Modifier
                .size(width = 104.dp, height = 78.dp)
                .clip(RoundedCornerShape(14.dp)),
        )
    }
}
