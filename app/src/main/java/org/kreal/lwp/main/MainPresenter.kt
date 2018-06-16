package org.kreal.lwp.main

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import org.kreal.lwp.models.IWallpaerManager

class MainPresenter(private val view: MainContract.View, private val wallpaperManager: IWallpaerManager) : MainContract.Presenter, IWallpaerManager.Callback {
    private val handler = Handler(Looper.getMainLooper())

    init {
        view.setPresenter(this)
        wallpaperManager.setChangeListener(this)
    }

    override fun start() {
        view.showLoading()
        view.showData(wallpaperManager.list())
    }

    override fun loadData() {
        postDataAsync(wallpaperManager.list())
    }

    override fun addPaper(context: Context, uri: Uri) {
        wallpaperManager.add(context, uri)
    }

    override fun deletePaper(vararg names: Uri) {
        wallpaperManager.delete(*names)
    }

    override fun changed(data: List<Uri>) {
        postDataAsync(data)
    }

    private fun postDataAsync(data: List<Uri>) {
        if (Thread.currentThread() == Looper.getMainLooper().thread)
            view.showData(data)
        else handler.post { view.showData(data) }
    }
}
