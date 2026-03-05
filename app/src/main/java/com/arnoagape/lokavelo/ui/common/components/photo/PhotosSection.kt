package com.arnoagape.lokavelo.ui.common.components.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImage
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.screen.owner.addBike.sections.SectionCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@Composable
fun PhotosSection(
    photos: List<PhotoItem>,
    photosError: Boolean,
    onAddPhotoClick: () -> Unit,
    onRemovePhoto: (String) -> Unit,
    onPhotoEdited: (String, Uri) -> Unit,
    onMovePhoto: (Int, Int) -> Unit,
    isEditable: Boolean = true
) {
    SectionCard(
        title = stringResource(R.string.pictures),
        subtitle = stringResource(R.string.subtitle_add_3_pictures)
    ) {
        if (photosError) {
            Text(
                text = stringResource(R.string.required),
                color = MaterialTheme.colorScheme.error
            )
        }

        PhotosContent(
            photos = photos,
            onAddPhotoClick = onAddPhotoClick,
            onRemovePhoto = onRemovePhoto,
            onPhotoEdited = onPhotoEdited,
            onMovePhoto = onMovePhoto,
            isEditable = isEditable
        )
    }
}

@Composable
fun PhotosContent(
    photos: List<PhotoItem>,
    onAddPhotoClick: () -> Unit,
    onRemovePhoto: (String) -> Unit,
    onPhotoEdited: (String, Uri) -> Unit,
    onMovePhoto: (Int, Int) -> Unit,
    isEditable: Boolean
) {

    var selectedPhotoId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedIndex = photos.indexOfFirst { it.id == selectedPhotoId }

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {

        if (isEditable) {

            val lazyListState = rememberLazyListState()

            val reorderState = rememberReorderableLazyListState(
                lazyListState = lazyListState,
                onMove = { from, to ->
                    onMovePhoto(from.index, to.index)
                }
            )

            LazyRow(
                state = lazyListState,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                items(
                    items = photos,
                    key = { it.id }
                ) { photo ->

                    ReorderableItem(
                        state = reorderState,
                        key = photo.id
                    ) {

                        PhotoPreview(
                            uri = photo.toUri(),
                            onRemoveClick = { onRemovePhoto(photo.id) },
                            onClick = { selectedPhotoId = photo.id },
                            modifier = Modifier.draggableHandle()
                        )
                    }
                }
            }

        } else {

            // MODE LECTURE SEULE → pas de reorder
            PhotoCarousel(
                photos = photos,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                onPhotoClick = { index ->
                    selectedPhotoId = photos[index].id
                }
            )
        }

        if (isEditable && photos.isNotEmpty()) {
            Spacer(modifier = Modifier.width(16.dp))
        }

        if (isEditable && photos.size < 3) {
            AddPhotoButton(onClick = onAddPhotoClick)
        }
    }

    // -------- Dialog --------

    val selectedPhoto = photos.find { it.id == selectedPhotoId }
    selectedPhoto?.let { photo ->

        val uri = when (photo) {
            is PhotoItem.Local -> photo.uri
            is PhotoItem.Remote -> photo.url.toUri()
        }

        if (isEditable) {
            PhotoEditorDialog(
                uri = uri,
                onDismiss = { selectedPhotoId = null },
                onValidate = { newUri ->
                    onPhotoEdited(photo.id, newUri)
                    selectedPhotoId = null
                }
            )
        } else {
            ZoomableImageViewer(
                uris = photos.map {
                    when (it) {
                        is PhotoItem.Local -> it.uri
                        is PhotoItem.Remote -> it.url.toUri()
                    }
                },
                startIndex = selectedIndex,
                onDismiss = { selectedPhotoId = null }
            )
        }
    }
}

private fun PhotoItem.toUri(): Uri =
    when (this) {
        is PhotoItem.Local -> uri
        is PhotoItem.Remote -> url.toUri()
    }

@Composable
fun PhotoPreview(
    modifier: Modifier = Modifier,
    uri: Uri,
    onRemoveClick: (() -> Unit)?,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
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
    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    var containerHeightPx by remember { mutableFloatStateOf(0f) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->

        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        scale = newScale

        val maxOffsetX = (containerWidthPx * (scale - 1f)) / 2f
        val maxOffsetY = (containerWidthPx * (scale - 1f)) / 2f

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
                .onGloballyPositioned { coordinates ->
                    containerWidthPx = coordinates.size.width.toFloat()
                    containerHeightPx = coordinates.size.height.toFloat()
                }
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
                        val cropSizePx = size.width

                        val left = (size.width - cropSizePx) / 2f
                        val top = (size.height - cropSizePx) / 2f
                        val right = left + cropSizePx
                        val bottom = top + cropSizePx

                        // Haut
                        drawRect(
                            color = overlayColor,
                            size = Size(size.width, top)
                        )

                        // Bas
                        drawRect(
                            color = overlayColor,
                            topLeft = Offset(0f, bottom),
                            size = Size(
                                size.width,
                                size.height - bottom
                            )
                        )

                        // Gauche
                        drawRect(
                            color = overlayColor,
                            topLeft = Offset(0f, top),
                            size = Size(left, cropSizePx)
                        )

                        // Droite
                        drawRect(
                            color = overlayColor,
                            topLeft = Offset(right, top),
                            size = Size(size.width - right, cropSizePx)
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

            // 🔝 Top bar
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                TextButton(
                    onClick = {

                        scope.launch {
                            isLoading = true

                            val newUri = withContext(Dispatchers.IO) {
                                when {
                                    isCropping -> {
                                        cropAndRotateImage(
                                            context,
                                            localUri!!,
                                            scale,
                                            offset,
                                            rotation,
                                            containerWidthPx,
                                            containerHeightPx
                                        )
                                    }

                                    rotation % 360f != 0f -> {
                                        rotateOnly(
                                            context,
                                            localUri!!,
                                            rotation
                                        )
                                    }

                                    else -> localUri!!
                                }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomableImageViewer(
    uris: List<Uri>,
    startIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { uris.size }
    )
    val scope = rememberCoroutineScope()

    var visible by remember { mutableStateOf(false) }
    var isZoomed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {

        Dialog(
            onDismissRequest = {
                visible = false
                onDismiss()
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {

                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = !isZoomed
                ) { page ->

                    var scale by remember { mutableFloatStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->

                        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                        scale = newScale

                        isZoomed = scale > 1f

                        val maxOffset = 800f * (scale - 1f)

                        val newOffset = offset + offsetChange

                        offset = Offset(
                            x = newOffset.x.coerceIn(-maxOffset, maxOffset),
                            y = newOffset.y.coerceIn(-maxOffset, maxOffset)
                        )
                    }

                    LaunchedEffect(scale) {
                        if (scale <= 1f) {
                            offset = Offset.Zero
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = uris[page],
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                                .transformable(transformState),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Flèche gauche
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(16.dp)
                            .windowInsetsPadding(WindowInsets.systemBars)
                            .background(
                                Color.Black,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_previous_picture),
                            tint = Color.White
                        )
                    }
                }

                // Flèche droite
                if (pagerState.currentPage < uris.lastIndex) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(16.dp)
                            .windowInsetsPadding(WindowInsets.systemBars)
                            .background(
                                Color.Black,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.cd_next_picture),
                            tint = Color.White
                        )
                    }
                }

                // Bouton X
                IconButton(
                    onClick = {
                        visible = false
                        onDismiss()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .windowInsetsPadding(WindowInsets.systemBars)
                        .background(
                            Color.Black,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

suspend fun ensureLocalImage(
    context: Context,
    uri: Uri
): Uri = withContext(Dispatchers.IO) {

    val inputStream = when (uri.scheme) {
        "http", "https" -> {
            val url = URL(uri.toString())
            url.openStream()
        }

        else -> {
            context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Cannot open input stream")
        }
    }

    val file = File(
        context.cacheDir,
        "local_${System.currentTimeMillis()}.jpg"
    )

    inputStream.use { input ->
        FileOutputStream(file).use { output ->
            input?.copyTo(output)
        }
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

    val original = context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it)
    } ?: throw IllegalStateException("Unable to decode bitmap")

    // ✅ Correction EXIF
    val orientation = context.contentResolver.openInputStream(uri)?.use {
        ExifInterface(it).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } ?: ExifInterface.ORIENTATION_NORMAL

    val exifMatrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
        }
    }

    val corrected = Bitmap.createBitmap(
        original,
        0,
        0,
        original.width,
        original.height,
        exifMatrix,
        true
    )

    if (corrected != original) {
        original.recycle()
    }

    // ✅ Rotation utilisateur
    val rotated = Bitmap.createBitmap(
        corrected,
        0,
        0,
        corrected.width,
        corrected.height,
        Matrix().apply { postRotate(rotation) },
        true
    )

    if (rotated != corrected) {
        corrected.recycle()
    }

    // ✅ Calcul crop
    val bitmapWidth = rotated.width.toFloat()
    val bitmapHeight = rotated.height.toFloat()

    val baseScale = minOf(
        containerWidthPx / bitmapWidth,
        containerHeightPx / bitmapHeight
    )

    val effectiveScale = baseScale * scale

    val cropSizeRaw = containerWidthPx / effectiveScale

    val cropSize = cropSizeRaw.coerceAtMost(
        minOf(bitmapWidth, bitmapHeight)
    )

    val centerX = bitmapWidth / 2f - offset.x / effectiveScale
    val centerY = bitmapHeight / 2f - offset.y / effectiveScale

    val maxLeft = (bitmapWidth - cropSize).coerceAtLeast(0f)
    val maxTop = (bitmapHeight - cropSize).coerceAtLeast(0f)

    val left = (centerX - cropSize / 2f)
        .coerceIn(0f, maxLeft)

    val top = (centerY - cropSize / 2f)
        .coerceIn(0f, maxTop)

    val cropped = Bitmap.createBitmap(
        rotated,
        left.toInt(),
        top.toInt(),
        cropSize.toInt(),
        cropSize.toInt()
    )

    if (rotated != cropped) {
        rotated.recycle()
    }

    val file = File(
        context.cacheDir,
        "cropped_${System.currentTimeMillis()}.jpg"
    )

    FileOutputStream(file).use {
        cropped.compress(Bitmap.CompressFormat.JPEG, 95, it)
    }

    cropped.recycle()

    return file.toUri()
}

fun rotateOnly(
    context: Context,
    uri: Uri,
    rotation: Float
): Uri {

    val original = context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it)
    } ?: throw IllegalStateException("Unable to decode bitmap")

    // ✅ Correction EXIF
    val orientation = context.contentResolver.openInputStream(uri)?.use {
        ExifInterface(it).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } ?: ExifInterface.ORIENTATION_NORMAL

    val matrix = Matrix().apply {

        // EXIF
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
        }

        // Rotation utilisateur
        postRotate(rotation)
    }

    val rotated = Bitmap.createBitmap(
        original,
        0,
        0,
        original.width,
        original.height,
        matrix,
        true
    )

    if (rotated != original) {
        original.recycle()
    }

    val file = File(
        context.cacheDir,
        "rotated_${System.currentTimeMillis()}.jpg"
    )

    FileOutputStream(file).use {
        rotated.compress(Bitmap.CompressFormat.JPEG, 95, it)
    }

    rotated.recycle()

    return file.toUri()
}