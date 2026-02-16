package com.arnoagape.lokavelo.ui.utils

import java.text.Normalizer

/**
 * Retrieves the display name of a file from a URI, falling back to its path segment.
 */
fun String.normalizeForSearch(): String {
    return Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .lowercase()
}