package io.github.halilozel1903.spaceflightnews.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

/**
 * A spaceflight news reader built on spaceflight-news-kotlin. It loads the live Spaceflight News API and the
 * articles' own images with Coil. `scripts/screenshots.sh` starts it with `--es scene <scene>` to open a fixed
 * screen, loaded live like a normal launch:
 *
 * - `feed`: the newest articles with the top story as a hero
 * - `search`: articles matching "Artemis"
 * - `reports`: the newest reports
 * - `tablet`: the article list and the newest article's detail side by side (tablets in landscape)
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val scene = Scene.from(intent.getStringExtra(EXTRA_SCENE))
        setContent {
            NewsTheme(dark = isSystemInDarkTheme()) {
                // The Surface makes text default to onBackground, so it stays readable in dark mode.
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                    ReaderApp(
                        scene = scene,
                        repository = if (scene != null) Repositories.scenes() else Repositories.live(),
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_SCENE = "scene"
    }
}
