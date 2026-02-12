package com.arnoagape.polyscribe.ui.common.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arnoagape.polyscribe.R
import com.arnoagape.polyscribe.ui.theme.PolyscribeTheme

/**
 * Displays a file item with an icon, file name and a remove button.
 *
 * @param fileName Name of the file to display.
 * @param icon Icon representing the file type.
 * @param onRemove Callback executed when the remove action is triggered.
 */
@Composable
fun FileRowItem(
    fileName: String,
    icon: ImageVector,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null)
                Text(
                    fileName,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(3f)
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.contentDescription_remove_file)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FileRowItemPreview() {
    PolyscribeTheme {
        FileRowItem(
            fileName = "example_lol_pdf_i_love_food_what_can_you.pdf",
            icon = Icons.Default.AttachFile,
            onRemove = {}
        )
    }
}