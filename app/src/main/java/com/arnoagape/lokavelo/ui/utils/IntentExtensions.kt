package com.arnoagape.polyscribe.ui.utils

import android.content.Intent
import android.os.Build
import android.os.Parcelable

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.parcelableExtra(name: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, T::class.java)
    } else {
        getParcelableExtra(name)
    }

@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.parcelableArrayListExtraCompat(
    name: String
): ArrayList<T>? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(name, T::class.java)
    } else {
        getParcelableArrayListExtra(name)
    }
