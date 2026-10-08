package tj.umar.navoplayer.core.designsystem.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import tj.umar.navoplayer.core.designsystem.R

object NavoIcons {
    val Search: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.navo_ic_search)

    val Settings: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.navo_ic_settings)

    val Sort: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.navo_ic_sort)

    val Shuffle: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.navo_ic_shuffle)

    val Folder: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.navo_ic_folder)

    val Play: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.navo_ic_play)

    val Pause: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.navo_ic_pause)
}
