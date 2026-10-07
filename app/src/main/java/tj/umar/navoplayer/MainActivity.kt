package tj.umar.navoplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.ui.NavoApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavoTheme {
                NavoApp()
            }
        }
    }
}
