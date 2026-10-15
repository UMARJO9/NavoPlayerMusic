package tj.umar.navoplayer

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.domain.model.ThemeMode
import tj.umar.navoplayer.core.domain.model.resolveDarkTheme
import tj.umar.navoplayer.feature.widget.navigation.isOpenNowPlayingRequest
import tj.umar.navoplayer.theme.syncApplicationNightMode
import tj.umar.navoplayer.ui.NavoApp

private const val LIGHT_NAVIGATION_FALLBACK_SCRIM = 0x8013204A.toInt()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    private val openNowPlayingRequests = Channel<Unit>(Channel.CONFLATED)
    private val openNowPlayingFlow = openNowPlayingRequests.receiveAsFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        applySystemBars(darkTheme = isNightConfiguration())
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value is MainActivityUiState.Loading }
        val fromHistory = (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0
        if (savedInstanceState == null && !fromHistory && intent.isOpenNowPlayingRequest()) {
            openNowPlayingRequests.trySend(Unit)
        }
        addOnNewIntentListener { newIntent ->
            if (newIntent.isOpenNowPlayingRequest()) openNowPlayingRequests.trySend(Unit)
        }
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            when (val state = uiState) {
                MainActivityUiState.Loading -> Unit
                is MainActivityUiState.Ready -> {
                    LaunchedEffect(state.themeMode) { syncApplicationNightMode(state.themeMode) }
                    ThemedApp(state.themeMode)
                }
            }
        }
    }

    @Composable
    private fun ThemedApp(themeMode: ThemeMode) {
        val darkTheme = themeMode.resolveDarkTheme(isSystemInDarkTheme())
        NavoTheme(darkTheme = darkTheme) {
            val background = NavoTheme.colors.background.toArgb()
            val uiMode = LocalConfiguration.current.uiMode
            LaunchedEffect(darkTheme, background, uiMode) {
                applySystemBars(darkTheme)
                window.setBackgroundDrawable(ColorDrawable(background))
            }
            NavoApp(openNowPlayingRequests = openNowPlayingFlow)
        }
    }

    private fun isNightConfiguration(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    private fun applySystemBars(darkTheme: Boolean) {
        if (darkTheme) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            )
        } else {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, LIGHT_NAVIGATION_FALLBACK_SCRIM),
            )
        }
    }
}
