package org.kreal.lwp.main

import android.content.Context
import android.net.Uri

interface MainContract {
    interface Presenter {
        fun start()

        fun loadData()

        fun addPaper(context: Context, uri: Uri)

        fun deletePaper(vararg names: Uri)
    }

    interface View {
        fun setPresenter(presenter: Presenter)

        fun showLoading()

        fun showData(data: List<Uri>)
    }
}