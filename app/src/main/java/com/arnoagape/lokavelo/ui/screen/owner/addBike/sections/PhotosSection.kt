package com.arnoagape.lokavelo.ui.screen.owner.addBike.sections

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.theme.LocalSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@Composable
fun PhotosSection(
    uris: List<Uri>,
    onAddPhotoClick: () -> Unit = {},
    onRemovePhoto: (Uri) -> Unit = {},
    onPhotoEdited: (Uri, Uri) -> Unit = { _, _ -> },
    isEditable: Boolean = true
) {
    SectionCard(
        title = stringResource(R.string.pictures),
        subtitle = stringResource(R.string.subtitle_add_3_pictures)
    ) {
        PhotosContent(
            uris = uris,
            onAddPhotoClick = onAddPhotoClick,
            onRemovePhoto = onRemovePhoto,
            onPhotoEdited = onPhotoEdited,
            isEditable = isEditable
        )
    }
}

@Composable
fun PhotosContent(
    uris: List<Uri>,
    onAddPhotoClick: () -> Unit = {},
    onRemovePhoto: (Uri) -> Unit = {},
    onPhotoEdited: (Uri, Uri) -> Unit = { _, _ -> },
    isEditable: Boolean = true
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val spacing = LocalSpacing.current

    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        modifier = Modifier.fillMaxWidth()
    ) {

        uris.forEach { uri ->

            PhotoPreview(
                uri = uri,
                onRemoveClick = if (isEditable) {
                    { onRemovePhoto(uri) }
                } else null,
                onClick = { selectedUri = uri }
            )
        }

        if (isEditable && uris.size < 3) {
            AddPhotoButton(
                onClick = onAddPhotoClick
            )
        }
    }

    selectedUri?.let { uri ->

        if (isEditable) {
            // Mode édition
            PhotoEditorDialog(
                uri = uri,
                onDismiss = { selectedUri = null },
                onValidate = { newUri ->
                    onPhotoEdited(uri, newUri)
                    selectedUri = null
                }
            )
        } else {
            // Mode détail (viewer simple)
            FullScreenImageViewer(
                uri = uri,
                onDismiss = { selectedUri = null }
            )
        }
    }
}

@Composable
fun PhotoPreview(
    uri: Uri,
    onRemoveClick: (() -> Unit)?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {

        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (onRemoveClick != null) {
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove_picture),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

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

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val density = LocalDensity.current
    val cropSizePx = with(density) { 300.dp.toPx() }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->

        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        scale = newScale

        val maxOffsetX = (cropSizePx * (scale - 1f)) / 2f
        val maxOffsetY = (cropSizePx * (scale - 1f)) / 2f

        val newOffset = offset + offsetChange

        offset = Offset(
            x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
            y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
        )
    }

    LaunchedEffect(scale) {
        if (scale <= 1f) {
            offset = Offset.Zero
        }
    }

    // 🔄 Téléchargement si nécessaire
    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            localUri = when (uri.scheme) {
                "http", "https" -> downloadImageToCache(context, uri)
                else -> uri
            }
        }
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            // ⏳ Loader pendant téléchargement
            if (isLoading || localUri == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
                return@Dialog
            }

            // 🖼️ Image preview
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
                            if (isCropping) {
                                Modifier.transformable(transformState)
                            } else {
                                Modifier
                            }
                        ),
                    contentScale = ContentScale.Fit
                )

                // 🎯 Cadre crop carré
                if (isCropping) {

                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        val overlayColor = Color.Black.copy(alpha = 0.6f)
                        val cropSizePx = 300.dp.toPx()

                        val left = (size.width - cropSizePx) / 2f
                        val top = (size.height - cropSizePx) / 2f
                        val right = left + cropSizePx
                        val bottom = top + cropSizePx

                        // Haut
                        drawRect(
                            color = overlayColor,
                            size = androidx.compose.ui.geometry.Size(size.width, top)
                        )

                        // Bas
                        drawRect(
                            color = overlayColor,
                            topLeft = Offset(0f, bottom),
                            size = androidx.compose.ui.geometry.Size(size.width, size.height - bottom)
                        )

                        // Gauche
                        drawRect(
                            color = overlayColor,
                            topLeft = Offset(0f, top),
                            size = androidx.compose.ui.geometry.Size(left, cropSizePx)
                        )

                        // Droite
                        drawRect(
                            color = overlayColor,
                            topLeft = Offset(right, top),
                            size = androidx.compose.ui.geometry.Size(size.width - right, cropSizePx)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .border(2.dp, Color.White)
                    )
                }
            }

            // 🔝 Top bar
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                TextButton(
                    onClick = {

                        if (!isCropping && rotation % 360f == 0f) {
                            onDismiss()
                            return@TextButton
                        }

                        scope.launch {
                            isLoading = true

                            val newUri = withContext(Dispatchers.IO) {
                                cropAndRotateImage(
                                    context,
                                    localUri!!,
                                    scale,
                                    offset,
                                    rotation
                                )
                            }

                            isLoading = false
                            onValidate(newUri)
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.validate),
                        color = Color.White
                    )
                }
            }

            // 🔻 Bottom actions
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {

                IconButton(
                    onClick = {
                        rotation = (rotation + 90f) % 360f
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = "Rotate",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        isCropping = !isCropping
                    }
                ) {
                    Icon(
                        Icons.Default.Crop,
                        contentDescription = "Crop",
                        tint = if (isCropping) Color.Green else Color.White
                    )
                }
            }
        }
    }
}

suspend fun downloadImageToCache(
    context: Context,
    uri: Uri
): Uri = withContext(Dispatchers.IO) {

    val url = URL(uri.toString())
    val connection = url.openConnection()
    connection.connect()

    val input = connection.getInputStream()

    val file = File(
        context.cacheDir,
        "temp_${System.currentTimeMillis()}.jpg"
    )

    FileOutputStream(file).use { output ->
        input.copyTo(output)
    }

    input.close()

    file.toUri()
}

@Composable
fun AddPhotoButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.add_picture),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun FullScreenImageViewer(
    uri: Uri,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->

        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        scale = newScale

        val maxOffset = 600f * (scale - 1f)

        val newOffset = offset + offsetChange

        offset = Offset(
            x = newOffset.x.coerceIn(-maxOffset, maxOffset),
            y = newOffset.y.coerceIn(-maxOffset, maxOffset)
        )
    }

    LaunchedEffect(scale, offset) {
        if (scale <= 1f) {
            offset = Offset.Zero
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = transformState),
                contentScale = ContentScale.Fit
            )
        }
    }
}

fun cropAndRotateImage(
    context: Context,
    uri: Uri,
    scale: Float,
    offset: Offset,
    rotation: Float
): Uri {

    val input = context.contentResolver.openInputStream(uri)
    val original = BitmapFactory.decodeStream(input)
    input?.close()

    // 1️⃣ Rotation d’abord
    val rotationMatrix = Matrix().apply {
        postRotate(rotation)
    }

    val rotated = Bitmap.createBitmap(
        original,
        0,
        0,
        original.width,
        original.height,
        rotationMatrix,
        true
    )

    // 2️⃣ Calcul du crop basé sur scale et offset

    val bitmapWidth = rotated.width.toFloat()
    val bitmapHeight = rotated.height.toFloat()

    val visibleWidth = bitmapWidth / scale
    val visibleHeight = bitmapHeight / scale

    val centerX = bitmapWidth / 2f - offset.x / scale
    val centerY = bitmapHeight / 2f - offset.y / scale

    val cropSize = minOf(visibleWidth, visibleHeight)

    val left = (centerX - cropSize / 2).coerceIn(0f, bitmapWidth - cropSize)
    val top = (centerY - cropSize / 2).coerceIn(0f, bitmapHeight - cropSize)

    val cropped = Bitmap.createBitmap(
        rotated,
        left.toInt(),
        top.toInt(),
        cropSize.toInt(),
        cropSize.toInt()
    )

    val file = File(
        context.cacheDir,
        "cropped_${System.currentTimeMillis()}.jpg"
    )

    FileOutputStream(file).use {
        cropped.compress(Bitmap.CompressFormat.JPEG, 95, it)
    }

    return file.toUri()
}