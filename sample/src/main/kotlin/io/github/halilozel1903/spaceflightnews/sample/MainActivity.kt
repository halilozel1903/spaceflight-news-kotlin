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
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import okhttp3.OkHttpClient

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
        // Images come from many news sites and CDNs; some (Wikimedia, for example) refuse requests without a
        // descriptive User-Agent.
        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components {
                    add(OkHttpNetworkFetcherFactory(callFactory = { imageClient }))
                }
                .build()
        }
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

        private val imageClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    chain.proceed(
                        chain.request().newBuilder()
                            .header("User-Agent", "spaceflight-news-kotlin-sample/1.0.0 (+https://github.com/halilozel1903/spaceflight-news-kotlin)")
                            .build(),
                    )
                }
                .build()
        }
    }
}
