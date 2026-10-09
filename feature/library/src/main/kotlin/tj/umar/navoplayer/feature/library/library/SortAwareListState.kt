package tj.umar.navoplayer.feature.library.library

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
internal fun rememberSortAwareListState(sortKey: Any): LazyListState {
    val listState = rememberLazyListState()
    var appliedKey by rememberSaveable { mutableStateOf(sortKey.toString()) }
    LaunchedEffect(sortKey) {
        val key = sortKey.toString()
        if (key != appliedKey) {
            appliedKey = key
            listState.scrollToItem(0)
        }
    }
    return listState
}
