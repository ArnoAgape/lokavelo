package com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.arnoagape.lokavelo.R

private const val TITLE_MAX_LENGTH = 60
private const val DESCRIPTION_MAX_LENGTH = 500

@Composable
fun TitleDescriptionSection(
    title: String,
    description: String,
    titleError: Boolean,
    descriptionError: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    SectionCard(
        title = stringResource(R.string.title_description),
        subtitle = stringResource(R.string.subtitle_title_description)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {

            OutlinedTextField(
                value = title,
                onValueChange = {
                    if (it.length <= TITLE_MAX_LENGTH) {
                        onTitleChange(it)
                    }
                },
                label = { Text(stringResource(R.string.title)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                isError = titleError,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(R.string.hint_title),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                },
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (titleError) {
                            Text(stringResource(R.string.required))
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Text(
                            text = "${title.length}/$TITLE_MAX_LENGTH",
                            color = when {
                                title.length > TITLE_MAX_LENGTH * 0.9 ->
                                    MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            )

            OutlinedTextField(
                value = description,
                onValueChange = {
                    if (it.length <= DESCRIPTION_MAX_LENGTH) {
                        onDescriptionChange(it)
                    }
                },
                label = { Text(stringResource(R.string.description)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                isError = descriptionError,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(R.string.hint_description),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                },
                minLines = 3,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (descriptionError) {
                            Text(stringResource(R.string.required))
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Text(
                            text = "${description.length}/$DESCRIPTION_MAX_LENGTH",
                            color = when {
                                description.length > DESCRIPTION_MAX_LENGTH * 0.9 ->
                                    MaterialTheme.colorScheme.error

                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            )

        }
    }
}