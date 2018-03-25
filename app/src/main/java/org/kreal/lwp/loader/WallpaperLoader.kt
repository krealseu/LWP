package org.kreal.lwp.loader

import android.content.AsyncTaskLoader
import android.content.Context
import android.net.Uri
import android.os.FileObserver
import android.os.Handler
import org.kreal.lwp.settings.WallpaperSource
import java.io.File
import java.util.regex.Pattern

/**
 * Created by lthee on 2017/10/1.
 * 从app的data下的WallpaperSource文件夹加载图片
 */
class WallpaperLoader(context: Context) : AsyncTaskLoader<List<Uri>>(context) {
    private val srcFile: File = File(context.filesDir, WallpaperSource)

    private var data: MutableList<Uri>? = null

    private val pattern: Pattern = Pattern.compile(".(jpg|jpeg|png)$", Pattern.CASE_INSENSITIVE)

    private val handler = Handler()

    private var lastDeliverTime: Long = 0

    private val delayTime: Long = 500

    private val contentChangedDelayRunnable = Runnable {
        onContentChanged()
    }

    private val fileObserver: FileObserver = object : FileObserver(srcFile.path, FileObserver.CREATE or FileObserver.DELETE) {
        override fun onEvent(event: Int, path: String?) {
            if (path != null && pattern.matcher(path).find()) {
                val currentTime = System.currentTimeMillis()
                val delta = if ((currentTime - lastDeliverTime) > delayTime) 0 else (currentTime - lastDeliverTime)
                handler.removeCallbacks(contentChangedDelayRunnable)
                handler.postDelayed(contentChangedDelayRunnable, delta)
            }
        }
    }

    init {
        fileObserver.startWatching()
    }

    override fun onStartLoading() {
//        data?.also { deliverResult(it) }
        if (takeContentChanged() || data == null) {
            forceLoad()
        }
    }

    override fun loadInBackground(): List<Uri> {
        val files = srcFile.listFiles { _, s -> pattern.matcher(s).find() }
                ?: arrayOf()
        data?.clear()
        val result = arrayListOf<Uri>()
        files.forEach { file ->
            result.add(Uri.fromFile(file))
        }
        data = result
        return result
    }

    override fun deliverResult(data: List<Uri>?) {
        if (isStarted) {
            lastDeliverTime = System.currentTimeMillis()
            super.deliverResult(data)
        }
    }

    override fun onReset() {
        super.onReset()
        fileObserver.stopWatching()
        data?.clear()
        data = null
    }
}