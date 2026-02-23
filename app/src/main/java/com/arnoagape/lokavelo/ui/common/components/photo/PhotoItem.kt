package com.arnoagape.lokavelo.ui.common.components.photo

import android.net.Uri

sealed class PhotoItem {
    abstract val id: String

    data class Remote(
        override val id: String,
        val url: String
    ) : PhotoItem()

    data class Local(
        override val id: String,
        val uri: Uri
    ) : PhotoItem()
}