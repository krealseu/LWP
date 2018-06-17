package org.kreal.lwp.main

import android.content.Context
import android.net.Uri
import org.kreal.lwp.models.IWallpaperManager

class MainPresenter(private val view: MainContract.View, private val wallpaperManager: IWallpaperManager) : MainContract.Presenter, IWallpaperManager.LoadWallpapersCallback {

    init {
        view.setPresenter(this)
    }

    override fun start() {
        view.showLoading()
        loadData()
    }

    override fun loadData() {
        wallpaperManager.list(this)
    }

    override fun addPaper(context: Context, uri: Uri) {
        view.showLoading()
        wallpaperManager.add(context, uri)
        loadData()
    }

    override fun deletePaper(vararg names: Uri) {
        view.showLoading()
        wallpaperManager.delete(*names)
        loadData()
    }

    override fun onWallpapersLoaded(data: List<Uri>) {
        view.showData(data)
    }

    override fun onDataNotAvailable() {
        view.showLoading()
    }
}
