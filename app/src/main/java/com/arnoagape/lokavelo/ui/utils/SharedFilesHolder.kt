package com.arnoagape.polyscribe.ui.utils

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf

object SharedFilesHolder {

    private val _uris = mutableStateListOf<Uri>()
    val uris: List<Uri> get() = _uris

    fun add(uri: Uri) {
        _uris.add(uri)
    }

    fun addAll(newUris: List<Uri>) {
        _uris.addAll(newUris)
    }

    fun consume(): List<Uri> {
        val result = _uris.toList()
        _uris.clear()
        return result
    }

    fun hasFiles(): Boolean = _uris.isNotEmpty()
}