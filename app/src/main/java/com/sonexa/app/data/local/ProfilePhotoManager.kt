package com.sonexa.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ProfilePhotoManager {

    private const val AVATARS_DIR = "avatars"
    private const val MAX_DIMENSION = 800
    private const val COMPRESS_QUALITY = 90

    suspend fun savePickedImage(context: Context, sourceUri: Uri, userId: String? = null): String? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val avatarDir = File(context.filesDir, AVATARS_DIR).apply { if (!exists()) mkdirs() }

            // Delete older avatars for cleanup
            val safeUserId = userId?.filter { it.isLetterOrDigit() }?.takeIf { it.isNotBlank() } ?: "current_user"
            avatarDir.listFiles { file -> file.name.startsWith("avatar_${safeUserId}") }?.forEach { it.delete() }

            // 1. Decode bounds first to prevent OOM on high-res camera photos
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            var inSampleSize = 1
            if (options.outHeight > MAX_DIMENSION || options.outWidth > MAX_DIMENSION) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= MAX_DIMENSION && halfWidth / inSampleSize >= MAX_DIMENSION) {
                    inSampleSize *= 2
                }
            }

            // 2. Decode scaled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val loadedBitmap = contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return@withContext null
            val rawBitmap: Bitmap = loadedBitmap

            // 3. Fix EXIF orientation if needed
            val rotationAngle = getRotationFromUri(context, sourceUri)
            val rotatedBitmap: Bitmap = if (rotationAngle != 0) {
                val matrix = Matrix().apply { postRotate(rotationAngle.toFloat()) }
                Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true).also {
                    if (it != rawBitmap) rawBitmap.recycle()
                }
            } else {
                rawBitmap
            }

            // 4. Center-crop to perfect square
            val squareBitmap: Bitmap = cropCenterSquare(rotatedBitmap)
            if (squareBitmap != rotatedBitmap) {
                rotatedBitmap.recycle()
            }

            // 5. Final resize to standard avatar size (e.g. 512x512)
            val finalBitmap: Bitmap = if (squareBitmap.width > 512) {
                Bitmap.createScaledBitmap(squareBitmap, 512, 512, true).also {
                    if (it != squareBitmap) squareBitmap.recycle()
                }
            } else {
                squareBitmap
            }

            // 6. Save to app files directory
            val destFile = File(avatarDir, "avatar_${safeUserId}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(destFile).use { outStream ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, outStream)
                outStream.flush()
            }
            finalBitmap.recycle()

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getRotationFromUri(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }

    private fun cropCenterSquare(srcBmp: Bitmap): Bitmap {
        return if (srcBmp.width >= srcBmp.height) {
            Bitmap.createBitmap(
                srcBmp,
                srcBmp.width / 2 - srcBmp.height / 2,
                0,
                srcBmp.height,
                srcBmp.height
            )
        } else {
            Bitmap.createBitmap(
                srcBmp,
                0,
                srcBmp.height / 2 - srcBmp.width / 2,
                srcBmp.width,
                srcBmp.width
            )
        }
    }

    fun clearLocalAvatar(context: Context, userId: String? = null) {
        try {
            val avatarDir = File(context.filesDir, AVATARS_DIR)
            val safeUserId = userId?.filter { it.isLetterOrDigit() }?.takeIf { it.isNotBlank() } ?: "current_user"
            avatarDir.listFiles { file -> file.name.startsWith("avatar_${safeUserId}") }?.forEach { it.delete() }
        } catch (_: Exception) {}
    }
}
