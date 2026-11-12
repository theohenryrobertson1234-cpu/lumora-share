
package com.lumoralabs.share.model

import android.graphics.Bitmap
import android.net.Uri

enum class FileCategory(val title: String) {
    APPS("Apps"),
    PHOTOS("Photos"),
    VIDEOS("Videos"),
    MUSIC("Music"),
    DOCUMENTS("Documents"),
    OTHER("Other")
}

data class ShareItem(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val category: FileCategory,
    val uri: Uri? = null,
    val mimeType: String? = null,
    val packageName: String? = null,
    val icon: Bitmap? = null,
    val modifiedMillis: Long = 0L
) {
    val sizeLabel: String
        get() {
            if (sizeBytes <= 0L) return "Size unavailable"

            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0

            return when {
                gb >= 1 -> String.format("%.2f GB", gb)
                mb >= 1 -> String.format("%.2f MB", mb)
                else -> String.format("%.1f KB", kb)
            }
        }
}
