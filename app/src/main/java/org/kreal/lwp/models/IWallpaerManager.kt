package org.kreal.lwp.models

import android.content.Context
import android.net.Uri

interface IWallpaerManager {

    fun list(): List<Uri>

    fun add(context: Context, uri: Uri)

    fun delete(vararg names: Uri)

    fun getRandom(): Uri

    fun setChangeListener(listener: Callback)

    interface Callback {
        fun changed(data: List<Uri>)
    }
}