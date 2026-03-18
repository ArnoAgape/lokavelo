package com.arnoagape.lokavelo.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

fun createImageUri(context: Context): Uri {
    val file = File(
        context.cacheDir,
        "camera_photo_${System.currentTimeMillis()}.jpg"
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

fun normalizeImage(context: Context, uri: Uri): Uri {
    val contentResolver = context.contentResolver

    val original = contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it)
    } ?: throw IllegalStateException("Cannot decode bitmap from $uri")

    val orientation = contentResolver.openInputStream(uri)?.use {
        ExifInterface(it).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } ?: ExifInterface.ORIENTATION_NORMAL

    val rotation = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90  -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }

    val corrected = if (rotation != 0f) {
        val matrix = Matrix().apply { postRotate(rotation) }
        Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
            .also { original.recycle() } // ✅ recycle garanti
    } else {
        original
    }

    return try {
        val file = File(context.cacheDir, "normalized_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            corrected.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        file.toUri()
    } finally {
        corrected.recycle() // ✅ recycle dans finally pour couvrir tous les cas
    }
}