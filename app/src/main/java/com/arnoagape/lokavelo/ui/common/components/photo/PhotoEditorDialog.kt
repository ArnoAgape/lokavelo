package com.arnoagape.lokavelo.ui.common.components.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import coil.compose.AsyncImage
import com.arnoagape.lokavelo.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@Composable
fun PhotoEditorDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onValidate: (Uri) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var rotation by remember { mutableFloatStateOf(0f) }
    var localUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isCropping by remember { mutableStateOf(false) }
    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    var containerHeightPx by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        val maxOffsetX = (containerWidthPx * (scale - 1f)) / 2f
        val maxOffsetY = (containerHeightPx * (scale - 1f)) / 2f  // ✅ était containerWidthPx
        offset = Offset(
            x = (offset.x + offsetChange.x).coerceIn(-maxOffsetX, maxOffsetX),
            y = (offset.y + offsetChange.y).coerceIn(-maxOffsetY, maxOffsetY)
        )
    }

    LaunchedEffect(scale) {
        if (scale <= 1f) offset = Offset.Zero
    }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                localUri = ensureLocalImage(context, uri)
            } catch (e: Exception) {
                e.printStackTrace()
                localUri = null
            } finally {
                isLoading = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .onGloballyPositioned {
                    containerWidthPx = it.size.width.toFloat()
                    containerHeightPx = it.size.height.toFloat()
                }
        ) {
            if (isLoading || localUri == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
                return@Dialog
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = localUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y,
                            rotationZ = rotation
                        )
                        .then(
                            if (isCropping) Modifier.transformable(transformState)
                            else Modifier
                        ),
                    contentScale = ContentScale.Fit
                )

                if (isCropping) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val overlayColor = Color.Black.copy(alpha = 0.6f)
                        val cropSize = size.width
                        val top = (size.height - cropSize) / 2f
                        val bottom = top + cropSize

                        drawRect(color = overlayColor, size = Size(size.width, top))
                        drawRect(
                            color = overlayColor,
                            topLeft = Offset(0f, bottom),
                            size = Size(size.width, size.height - bottom)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .border(2.dp, Color.White)
                    )
                }
            }

            // Top bar
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            val newUri = withContext(Dispatchers.IO) {
                                when {
                                    isCropping -> cropAndRotateImage(
                                        context, localUri!!, scale, offset,
                                        rotation, containerWidthPx, containerHeightPx
                                    )
                                    rotation % 360f != 0f -> rotateOnly(context, localUri!!, rotation)
                                    else -> localUri!!
                                }
                            }
                            isLoading = false
                            onValidate(newUri)
                        }
                    }
                ) {
                    Text(stringResource(R.string.validate), color = Color.White)
                }
            }

            // Bottom actions
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                IconButton(onClick = { rotation = (rotation + 90f) % 360f }) {
                    Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, tint = Color.White)
                }
                IconButton(onClick = { isCropping = !isCropping }) {
                    Icon(
                        Icons.Default.Crop,
                        contentDescription = null,
                        tint = if (isCropping) Color.Green else Color.White
                    )
                }
            }
        }
    }
}

// ✅ Une seule lecture grâce au fichier local (ExifInterface accepte un File)
private fun readBitmapAndOrientation(file: File): Pair<Bitmap, Int> {
    val bitmap = BitmapFactory.decodeFile(file.path)
        ?: throw IllegalStateException("Unable to decode bitmap")
    val orientation = ExifInterface(file.absolutePath).getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    return bitmap to orientation
}

private fun Matrix.applyExifOrientation(orientation: Int) {
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
    }
}

suspend fun ensureLocalImage(context: Context, uri: Uri): Uri =
    withContext(Dispatchers.IO) {
        val inputStream = when (uri.scheme) {
            "http", "https" -> URL(uri.toString()).openStream()
            else -> context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Cannot open input stream")
        }
        val file = File(context.cacheDir, "local_${System.currentTimeMillis()}.jpg")
        inputStream.use { input ->
            FileOutputStream(file).use { input?.copyTo(it) }
        }
        file.toUri()
    }

fun cropAndRotateImage(
    context: Context,
    uri: Uri,
    scale: Float,
    offset: Offset,
    rotation: Float,
    containerWidthPx: Float,
    containerHeightPx: Float
): Uri {
    // ✅ uri est déjà local (ensureLocalImage garanti) → on lit le fichier directement
    val file = File(uri.path!!)
    val (original, orientation) = readBitmapAndOrientation(file)

    val exifMatrix = Matrix().apply { applyExifOrientation(orientation) }
    val corrected = Bitmap.createBitmap(original, 0, 0, original.width, original.height, exifMatrix, true)
        .also { if (it != original) original.recycle() }

    val rotated = Bitmap.createBitmap(corrected, 0, 0, corrected.width, corrected.height,
        Matrix().apply { postRotate(rotation) }, true)
        .also { if (it != corrected) corrected.recycle() }

    val bitmapWidth = rotated.width.toFloat()
    val bitmapHeight = rotated.height.toFloat()
    val effectiveScale = minOf(containerWidthPx / bitmapWidth, containerHeightPx / bitmapHeight) * scale
    val cropSize = (containerWidthPx / effectiveScale).coerceAtMost(minOf(bitmapWidth, bitmapHeight))

    val centerX = bitmapWidth / 2f - offset.x / effectiveScale
    val centerY = bitmapHeight / 2f - offset.y / effectiveScale
    val left = (centerX - cropSize / 2f).coerceIn(0f, (bitmapWidth - cropSize).coerceAtLeast(0f))
    val top = (centerY - cropSize / 2f).coerceIn(0f, (bitmapHeight - cropSize).coerceAtLeast(0f))

    val cropped = Bitmap.createBitmap(rotated, left.toInt(), top.toInt(), cropSize.toInt(), cropSize.toInt())
        .also { if (it != rotated) rotated.recycle() }

    return saveToCache(context, cropped, "cropped")
}

fun rotateOnly(context: Context, uri: Uri, rotation: Float): Uri {
    val file = File(uri.path!!)
    val (original, orientation) = readBitmapAndOrientation(file)

    val rotated = Bitmap.createBitmap(original, 0, 0, original.width, original.height,
        Matrix().apply {
            applyExifOrientation(orientation)
            postRotate(rotation)
        }, true
    ).also { if (it != original) original.recycle() }

    return saveToCache(context, rotated, "rotated")
}

// ✅ Factorisation de la sauvegarde en cache
private fun saveToCache(context: Context, bitmap: Bitmap, prefix: String): Uri {
    val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
    bitmap.recycle()
    return file.toUri()
}