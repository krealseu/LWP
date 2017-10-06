package org.kreal.lwp.loader

import android.content.AsyncTaskLoader
import android.content.Context
import android.database.Cursor
import org.kreal.lwp.SQLDatabase.WallpaperManager
import org.kreal.lwp.SQLDatabase.WallpaperSQL

/**
 * Created by lthee on 2017/10/3.
 */
class WPCursorLoader(context: Context?) : AsyncTaskLoader<Cursor>(context) {
    override fun loadInBackground(): Cursor {
        var wallpaperManager = WallpaperManager(context)
        return wallpaperManager.query(Array(1) { WallpaperSQL.DATA }, null, null, null, null, null)
    }

    override fun onStartLoading() {
        super.onStartLoading()
        forceLoad()
    }
}