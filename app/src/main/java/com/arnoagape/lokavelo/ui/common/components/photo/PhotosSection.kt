package com.arnoagape.lokavelo.ui.common.components.photo

import android.net.Uri
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.arnoagape.lokavelo.R
import com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections.SectionCard
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private fun PhotoItem.toUri(): Uri =
    when (this) {
        is PhotoItem.Local -> uri
        is PhotoItem.Remote -> url.toUri()
    }

@Composable
fun PhotosSection(
    photos: List<PhotoItem>,
    photosError: Boolean,
    onAddPhotoClick: () -> Unit,
    onRemovePhoto: (String) -> Unit,
    onPhotoEdited: (String, Uri) -> Unit,
    onMovePhoto: (Int, Int) -> Unit,
    onViewerOpen: ((List<Uri>, Int) -> Unit)? = null,
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
            onViewerOpen = onViewerOpen,
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
    onViewerOpen: ((List<Uri>, Int) -> Unit)? = null,
    isEditable: Boolean
) {

    var selectedPhotoId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedIndex = photos.indexOfFirst { it.id == selectedPhotoId }.coerceAtLeast(0)

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isEditable && photos.size < 3) {
            AddPhotoButton(onClick = onAddPhotoClick)
        }

        if (isEditable && photos.isNotEmpty()) {
            Spacer(modifier = Modifier.width(16.dp))
        }

        if (isEditable) {
            val lazyListState = rememberLazyListState()
            val reorderState = rememberReorderableLazyListState(
                lazyListState = lazyListState,
                onMove = { from, to -> onMovePhoto(from.index, to.index) }
            )

            LazyRow(
                modifier = Modifier.weight(1f),
                state = lazyListState,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = photos, key = { it.id }) { photo ->
                    ReorderableItem(state = reorderState, key = photo.id) {
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
            PhotoCarousel(
                photos = photos,
                modifier = Modifier.fillMaxWidth().height(220.dp),
                onPhotoClick = { index -> selectedPhotoId = photos[index].id }
            )
        }
    }

    // -------- Dialog --------

    val selectedPhoto = photos.find { it.id == selectedPhotoId }
    selectedPhoto?.let { photo ->

        val uri = photo.toUri()

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
            // 👇 Si onViewerOpen fourni → délègue au parent
            if (onViewerOpen != null) {
                if (selectedPhotoId != null) {
                    SideEffect {
                        onViewerOpen(photos.map { it.toUri() }, selectedIndex)
                    }
                }
            } else {
                // Mode autonome
                ZoomableImageViewer(
                    uris = photos.map { it.toUri() },
                    startIndex = selectedIndex,
                    onDismiss = { selectedPhotoId = null }
                )
            }
        }
    }
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