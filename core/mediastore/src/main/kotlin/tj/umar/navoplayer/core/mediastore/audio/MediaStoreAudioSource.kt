package tj.umar.navoplayer.core.mediastore.audio

import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.os.OperationCanceledException
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import tj.umar.navoplayer.core.common.dispatchers.IoDispatcher
import javax.inject.Inject

internal class MediaStoreAudioSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AudioMediaSource {

    private val collection: Uri = audioCollection()

    override fun observeChanges(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        context.contentResolver.registerContentObserver(collection, true, observer)
        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }.buffer(Channel.CONFLATED)

    override suspend fun queryAudio(): List<MediaStoreAudioRow> = withContext(ioDispatcher) {
        val cancellationSignal = CancellationSignal()
        val cancelHandle = coroutineContext.job.invokeOnCompletion { cancellationSignal.cancel() }
        val cursor = try {
            context.contentResolver.query(
                collection,
                projection(),
                "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                null,
                null,
                cancellationSignal,
            )
        } catch (e: OperationCanceledException) {
            ensureActive()
            throw e
        } finally {
            cancelHandle.dispose()
        } ?: return@withContext emptyList()

        cursor.use {
            val columns = AudioColumns(it)
            val rows = ArrayList<MediaStoreAudioRow>(it.count)
            while (it.moveToNext()) {
                if (rows.size % CANCELLATION_CHECK_INTERVAL == 0) ensureActive()
                rows += columns.read(it)
            }
            rows
        }
    }

    private inner class AudioColumns(cursor: Cursor) {
        private val id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        private val title = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        private val displayName = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        private val artist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        private val artistId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
        private val album = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        private val albumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        private val duration = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
        private val track = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
        private val dateAdded = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
        private val relativePath = cursor.getColumnIndex(RELATIVE_PATH_COLUMN)
        private val dataPath = cursor.getColumnIndex(DATA_COLUMN)
        private val albumArtist = cursor.getColumnIndex(ALBUM_ARTIST_COLUMN)

        fun read(cursor: Cursor): MediaStoreAudioRow {
            val rowId = cursor.getLong(id)
            return MediaStoreAudioRow(
                id = rowId,
                title = cursor.stringOrNull(title),
                displayName = cursor.stringOrNull(displayName),
                artist = cursor.stringOrNull(artist),
                artistId = cursor.longOrNull(artistId),
                album = cursor.stringOrNull(album),
                albumId = cursor.longOrNull(albumId),
                durationMs = cursor.longOrNull(duration),
                track = cursor.intOrNull(track),
                contentUri = ContentUris.withAppendedId(collection, rowId).toString(),
                relativePath = cursor.optionalString(relativePath),
                dataPath = cursor.optionalString(dataPath),
                albumArtist = cursor.optionalString(albumArtist),
                dateAddedSeconds = cursor.longOrNull(dateAdded),
            )
        }
    }

    private companion object {
        const val CANCELLATION_CHECK_INTERVAL = 200
        const val RELATIVE_PATH_COLUMN = "relative_path"
        const val DATA_COLUMN = "_data"
        const val ALBUM_ARTIST_COLUMN = "album_artist"

        val BASE_PROJECTION = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
        )

        fun projection(): Array<String> {
            val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) RELATIVE_PATH_COLUMN else DATA_COLUMN
            val withLocation = BASE_PROJECTION + location
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) withLocation + ALBUM_ARTIST_COLUMN else withLocation
        }

        fun audioCollection(): Uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
    }
}

private fun Cursor.stringOrNull(index: Int): String? = if (isNull(index)) null else getString(index)

private fun Cursor.longOrNull(index: Int): Long? = if (isNull(index)) null else getLong(index)

private fun Cursor.intOrNull(index: Int): Int? = if (isNull(index)) null else getInt(index)

private fun Cursor.optionalString(index: Int): String? = if (index < 0) null else stringOrNull(index)
