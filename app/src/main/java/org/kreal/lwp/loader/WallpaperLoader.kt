package org.kreal.lwp.loader

import android.content.AsyncTaskLoader
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File

/**
 * Created by lthee on 2017/10/1.
 */
class WallpaperLoader(context: Context?) : AsyncTaskLoader<Array<String>>(context) {
    lateinit var mFiles: Array<File>
    var mRoot: File

    init {
        mRoot = File("/sdcard/CC/BiZhi")
    }

    override fun loadInBackground(): Array<String> {
        var filenames = mRoot.list { _, s -> s.endsWith(".jpg") || s.endsWith(".png") }
        for (i in 0..(filenames.size - 1))
            filenames[i] = mRoot.canonicalPath + File.separator + filenames[i]
        return filenames
    }

    override fun deliverResult(data: Array<String>?) {
        super.deliverResult(data)
    }

    override fun onStartLoading() {
//        if (mFiles!=null)
//            deliverResult(mFiles)
        super.onStartLoading()
        forceLoad()
    }
}