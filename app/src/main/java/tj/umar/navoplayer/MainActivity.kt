package tj.umar.navoplayer

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.feature.widget.navigation.isOpenNowPlayingRequest
import tj.umar.navoplayer.ui.NavoApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val openNowPlayingRequests = Channel<Unit>(Channel.CONFLATED)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        if (savedInstanceState == null && intent.isOpenNowPlayingRequest()) openNowPlayingRequests.trySend(Unit)
        addOnNewIntentListener { newIntent ->
            if (newIntent.isOpenNowPlayingRequest()) openNowPlayingRequests.trySend(Unit)
        }
        setContent {
            NavoTheme {
                NavoApp(openNowPlayingRequests = openNowPlayingRequests.receiveAsFlow())
            }
        }
    }
}
