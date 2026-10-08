package tj.umar.navoplayer.core.player.controller

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tj.umar.navoplayer.core.common.coroutines.ApplicationScope
import tj.umar.navoplayer.core.common.dispatchers.MainDispatcher
import tj.umar.navoplayer.core.player.service.PlaybackService
import javax.inject.Inject
import javax.inject.Singleton

private const val RELEASE_GRACE_MILLIS = 5_000L

@Singleton
internal class MediaControllerConnection @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val scope: CoroutineScope,
) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var users = 0
    private var releaseJob: Job? = null
    private val disconnects = MutableSharedFlow<MediaController>(extraBufferCapacity = 8)

    fun <T> withController(block: (MediaController) -> Flow<T>): Flow<T> = channelFlow {
        while (true) {
            val controller = acquire()
            try {
                if (!controller.isConnected) {
                    if (controllerFuture?.resolvedController() === controller) controllerFuture = null
                    continue
                }
                coroutineScope {
                    val forwarding = launch { block(controller).collect { send(it) } }
                    disconnects.first { it === controller }
                    forwarding.cancel()
                }
            } finally {
                release()
            }
        }
    }.flowOn(mainDispatcher)

    suspend fun <T> command(block: (MediaController) -> T): T = withContext(mainDispatcher) {
        val controller = acquire()
        try {
            block(controller)
        } finally {
            release()
        }
    }

    private suspend fun acquire(): MediaController {
        releaseJob?.cancel()
        releaseJob = null
        users++
        val future = controllerFuture ?: buildController().also { controllerFuture = it }
        return try {
            Futures.nonCancellationPropagating(future).await()
        } catch (failure: Throwable) {
            if (controllerFuture === future && future.isDone) controllerFuture = null
            release()
            throw failure
        }
    }

    private fun release() {
        users = (users - 1).coerceAtLeast(0)
        if (users > 0) return
        val future = controllerFuture ?: return
        releaseJob = scope.launch(mainDispatcher) {
            delay(RELEASE_GRACE_MILLIS)
            if (users == 0 && controllerFuture === future) {
                controllerFuture = null
                MediaController.releaseFuture(future)
            }
        }
    }

    private fun buildController(): ListenableFuture<MediaController> {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        return MediaController.Builder(context, token)
            .setListener(object : MediaController.Listener {
                override fun onDisconnected(controller: MediaController) {
                    if (controllerFuture?.resolvedController() === controller) controllerFuture = null
                    disconnects.tryEmit(controller)
                }
            })
            .buildAsync()
    }
}

private fun ListenableFuture<MediaController>.resolvedController(): MediaController? =
    if (isDone && !isCancelled) runCatching { get() }.getOrNull() else null
