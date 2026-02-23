package com.arnoagape.lokavelo.ui.screen.owner.addBike.sections

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
                onClick = {
                    if (isEditable) {
                        selectedUri = uri
                    }
                }
            )
        }

        if (isEditable && uris.size < 3) {
            AddPhotoButton(
                onClick = onAddPhotoClick
            )
        }
    }

    // 👇 Photo Editor Dialog
    selectedUri?.let { uri ->

        PhotoEditorDialog(
            uri = uri,
            onDismiss = { selectedUri = null },
            onValidate = { newUri ->
                onPhotoEdited(uri, newUri)
                selectedUri = null
            }
        )
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
            AsyncImage(
                model = localUri.toString() + "?t=${System.currentTimeMillis()}",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = rotation
                    },
                contentScale = ContentScale.Fit
            )

            // 🔝 Top bar
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                TextButton(
                    onClick = {

                        if (rotation % 360f == 0f) {
                            onDismiss()
                            return@TextButton
                        }

                        scope.launch {
                            isLoading = true

                            val newUri = withContext(Dispatchers.IO) {
                                rotateImageAndSave(
                                    context,
                                    localUri!!,
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
                        // TODO Crop plus tard
                    }
                ) {
                    Icon(
                        Icons.Default.Crop,
                        contentDescription = "Crop",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

fun rotateImageAndSave(
    context: Context,
    uri: Uri,
    rotation: Float
): Uri {

    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)

    val matrix = Matrix().apply {
        postRotate(rotation)
    }

    val rotatedBitmap = Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true
    )

    val file = File(
        context.cacheDir,
        "edited_${System.currentTimeMillis()}.jpg"
    )

    val outputStream = FileOutputStream(file)
    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)

    outputStream.flush()
    outputStream.close()

    return file.toUri()
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

        if (scale > 1f) {
            offset += offsetChange
        } else {
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