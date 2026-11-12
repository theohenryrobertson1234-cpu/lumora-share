
package com.lumoralabs.share.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.BaseColumns
import com.lumoralabs.share.model.FileCategory
import com.lumoralabs.share.model.ShareItem
import java.io.File

class DeviceFileRepository(context: Context) {

    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager
    private val resolver = appContext.contentResolver

    fun getInstalledApps(): List<ShareItem> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        @Suppress("DEPRECATION")
        val activities = if (Build.VERSION.SDK_INT >= 33) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            packageManager.queryIntentActivities(intent, 0)
        }

        return activities
            .distinctBy { it.activityInfo?.packageName }
            .mapNotNull { result ->
                try {
                    val appInfo = result.activityInfo?.applicationInfo
                        ?: return@mapNotNull null

                    val appName = result.loadLabel(packageManager).toString()
                    val packageName = appInfo.packageName

                    val baseSize = File(appInfo.sourceDir).length()
                    val splitSize = appInfo.splitSourceDirs
                        ?.sumOf { File(it).length() } ?: 0L

                    ShareItem(
                        id = packageName,
                        name = appName,
                        sizeBytes = baseSize + splitSize,
                        category = FileCategory.APPS,
                        packageName = packageName,
                        icon = drawableToBitmap(
                            result.loadIcon(packageManager)
                        )
                    )
                } catch (_: Exception) {
                    null
                }
            }
            .sortedBy { it.name.lowercase() }
    }

    fun getPhotos(): List<ShareItem> {
        return queryMedia(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            FileCategory.PHOTOS
        )
    }

    fun getVideos(): List<ShareItem> {
        return queryMedia(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            FileCategory.VIDEOS
        )
    }

    fun getMusic(): List<ShareItem> {
        return queryMedia(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            FileCategory.MUSIC
        )
    }

    fun getDocuments(): List<ShareItem> {
        return queryGeneralFiles().filter {
            isDocument(it.name, it.mimeType)
        }
    }

    fun getOtherFiles(): List<ShareItem> {
        return queryGeneralFiles().filter {
            !isDocument(it.name, it.mimeType) &&
                !isMediaFile(it.mimeType)
        }
    }

    private fun queryMedia(
        collection: Uri,
        category: FileCategory
    ): List<ShareItem> {
        val results = mutableListOf<ShareItem>()

        val projection = arrayOf(
            BaseColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )

        try {
            resolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->

                val idColumn = cursor.getColumnIndexOrThrow(BaseColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.MediaColumns.DISPLAY_NAME
                )
                val sizeColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.MediaColumns.SIZE
                )
                val mimeColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.MediaColumns.MIME_TYPE
                )
                val dateColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.MediaColumns.DATE_MODIFIED
                )

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Unknown file"
                    val size = cursor.getLong(sizeColumn)
                    val mime = cursor.getString(mimeColumn)
                    val modified = cursor.getLong(dateColumn)

                    results.add(
                        ShareItem(
                            id = "${category.name}_$id",
                            name = name,
                            sizeBytes = size,
                            category = category,
                            uri = Uri.withAppendedPath(
                                collection,
                                id.toString()
                            ),
                            mimeType = mime,
                            modifiedMillis = modified * 1000L
                        )
                    )
                }
            }
        } catch (_: SecurityException) {
            return emptyList()
        } catch (_: Exception) {
            return emptyList()
        }

        return results
    }

    private fun queryGeneralFiles(): List<ShareItem> {
        val results = mutableListOf<ShareItem>()

        val collection = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            BaseColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )

        try {
            resolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->

                val idColumn = cursor.getColumnIndexOrThrow(BaseColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.MediaColumns.DISPLAY_NAME
                )
                val sizeColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.MediaColumns.SIZE
                )
                val mimeColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.MediaColumns.MIME_TYPE
                )
                val dateColumn = cursor.getColumnIndexOrThrow(
                    MediaStore.MediaColumns.DATE_MODIFIED
                )

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: continue
                    val mime = cursor.getString(mimeColumn)
                    val size = cursor.getLong(sizeColumn)
                    val modified = cursor.getLong(dateColumn)

                    results.add(
                        ShareItem(
                            id = "FILE_$id",
                            name = name,
                            sizeBytes = size,
                            category = if (isDocument(name, mime)) {
                                FileCategory.DOCUMENTS
                            } else {
                                FileCategory.OTHER
                            },
                            uri = Uri.withAppendedPath(collection, id.toString()),
                            mimeType = mime,
                            modifiedMillis = modified * 1000L
                        )
                    )
                }
            }
        } catch (_: SecurityException) {
            return emptyList()
        } catch (_: Exception) {
            return emptyList()
        }

        return results
    }

    private fun isDocument(
        name: String,
        mimeType: String?
    ): Boolean {
        val extension = name.substringAfterLast(
            ".",
            ""
        ).lowercase()

        val documentExtensions = setOf(
            "pdf", "doc", "docx", "xls", "xlsx",
            "ppt", "pptx", "txt", "rtf", "csv",
            "odt", "ods", "odp", "epub"
        )

        return extension in documentExtensions ||
            mimeType?.startsWith("application/") == true ||
            mimeType?.startsWith("text/") == true
    }

    private fun isMediaFile(mimeType: String?): Boolean {
        return mimeType?.startsWith("image/") == true ||
            mimeType?.startsWith("video/") == true ||
            mimeType?.startsWith("audio/") == true
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            drawable.bitmap?.let { return it }
        }

        val width = drawable.intrinsicWidth
            .takeIf { it > 0 } ?: 96

        val height = drawable.intrinsicHeight
            .takeIf { it > 0 } ?: 96

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}
