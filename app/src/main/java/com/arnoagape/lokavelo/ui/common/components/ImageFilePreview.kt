package com.arnoagape.polyscribe.ui.common.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.arnoagape.polyscribe.R

/**
 * Displays a preview of a document based on its type (image/PDF/other).
 * On detail screens, clicking an image opens it in a fullscreen dialog.
 *
 * @param modifier Optional layout modifier.
 * @param documentUrl URL of the file to preview.
 * @param isDetailScreen Whether the preview is shown on a detail screen.
 * @param onClick Optional callback when the preview is clicked (non-detail only).
 */
@Composable
fun ImageFilePreview(
    modifier: Modifier = Modifier,
    documentUrl: String,
    isDetailScreen: Boolean,
    onClick: (() -> Unit)? = null
) {

    var selectedImage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val baseUrl = documentUrl.substringBefore("?")

    val isImage =
        baseUrl.endsWith(".jpg", ignoreCase = true) ||
                baseUrl.endsWith(".jpeg", ignoreCase = true) ||
                baseUrl.endsWith(".png", ignoreCase = true)

    val isPdf = baseUrl.endsWith(".pdf", ignoreCase = true)

    val previewHeight = if (isDetailScreen) 150.dp else 100.dp

    when {
        isImage -> {
            AsyncImage(
                modifier = modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(previewHeight)
                    .clickable {
                        if (isDetailScreen) selectedImage = documentUrl
                        else onClick?.invoke()
                    },
                model = documentUrl,
                placeholder = ColorPainter(Color.DarkGray),
                contentDescription = stringResource(R.string.contentDescription_file_preview),
                contentScale = ContentScale.Crop
            )

            if (isDetailScreen && selectedImage != null) {
                Dialog(
                    onDismissRequest = { selectedImage = null }, // closes when click
                    properties = DialogProperties(usePlatformDefaultWidth = false) // full screen
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .clickable { selectedImage = null }, // closes when click
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = selectedImage,
                            contentDescription = stringResource(R.string.contentDescription_file_preview),
                            modifier = Modifier.fillMaxSize()
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                                .padding(8.dp)
                                .clickable { selectedImage = null }
                        )
                    }
                }
            }
        }

        isPdf -> {
            if (isDetailScreen) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = stringResource(R.string.contentDescription_file_preview_pdf),
                    modifier = modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(previewHeight)
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(documentUrl.toUri(), "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                }

                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.no_pdf_app),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (_: Exception) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.error_open_pdf),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = stringResource(R.string.contentDescription_file_preview_pdf),
                    modifier = modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(previewHeight)
                        .clickable { onClick?.invoke() }
                )
            }
        }

        else -> {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = stringResource(R.string.contentDescription_file_preview),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(previewHeight)
            )
        }
    }
}

/**
 * Displays a list of previews for provided file URLs.
 * On non-detail screens, only the first preview is displayed.
 *
 * @param fileUrls List of document URLs to preview.
 * @param isDetailScreen Whether full preview list should be shown.
 * @param onClick Optional callback when a preview is clicked.
 */
@Composable
fun FilePreviewList(
    fileUrls: List<String>,
    isDetailScreen: Boolean,
    onClick: (() -> Unit)? = null
) {

    if (fileUrls.isEmpty()) return

    val urlsToDisplay = if (isDetailScreen) fileUrls else listOf(fileUrls.first())

    urlsToDisplay.forEach { url ->
        ImageFilePreview(
            documentUrl = url,
            isDetailScreen = isDetailScreen,
            onClick = onClick
        )
    }
}