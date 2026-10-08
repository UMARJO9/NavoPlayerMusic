package tj.umar.navoplayer.core.mediastore.audio

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class FakeMediaProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        lastSelection = selection
        lastProjection = projection?.toList()
        val columns = projection ?: rows.firstOrNull()?.keys?.toTypedArray() ?: emptyArray()
        return MatrixCursor(columns).apply {
            rows.forEach { row -> addRow(columns.map { row[it] }) }
        }
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    companion object {
        var rows: List<Map<String, Any?>> = emptyList()
        var lastSelection: String? = null
        var lastProjection: List<String>? = null
    }
}
