package org.kreal.lwp.models

import android.content.Context
import android.net.Uri

interface IWallpaperManager {

    fun list(callback: LoadWallpapersCallback)

    fun add(context: Context, uri: Uri)

    fun delete(vararg names: Uri)

    fun getRandom(): Uri

    interface LoadWallpapersCallback {
        fun onWallpapersLoaded(data: List<Uri>)
        fun onDataNotAvailable()
    }

    interface GetWallpaperCallback {
        fun onWallpaperGeted(uri: Uri)

        fun onDataNotAvailable()
    }
}