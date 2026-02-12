package com.arnoagape.polyscribe.ui.common

import android.net.Uri
import java.time.Instant

/**
 * A sealed class representing different events that can occur on a form.
 */
sealed class FormEvent {

    data class DateTimeChanged(val dateTime: Instant) : FormEvent()
    data class ColorChanged(val colored: Boolean) : FormEvent()
    data class DoubleSidedChanged(val doubleSided: Boolean) : FormEvent()
    data class NumberOfCopiesSet(val value: Int) : FormEvent()
    data class AddFile(val uri: Uri) : FormEvent()
    data class RemoveFile(val uri: Uri) : FormEvent()
    data class CommentChanged(val comment: String) : FormEvent()
    data class DisplayNameChanged(val displayName: String) : FormEvent()
    data class EmailChanged(val email: String) : FormEvent()

}