package org.kreal.lwp.models

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager


import java.io.File
import java.io.FilenameFilter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Random

/**
 * Created by lthee on 2017/5/27.
 * 管理壁纸文件加和获取一张壁纸
 */

class FileManager(context: Context) {
    private val mSharedPreferences: SharedPreferences
    private val mPrfKey: String? = null
    private var fileroot: String = "/sdcard/CC/BiZhi"
    private var mLastWallpaper = ""
    private val DefaultWallpaper: String

    val randomWallpaper: String
        get() {
            val paperfolder = File(fileroot)
            if (!paperfolder.isDirectory)
                return DefaultWallpaper
            val papers = paperfolder.list { _, filename -> filename.endsWith(".jpg") || filename.endsWith(".png") }
            if (papers == null || papers.isEmpty())
                return DefaultWallpaper
            var i: Int
            if (papers.size == 1)
                i = 0
            else {
                do {
                    i = Random().nextInt(papers.size)
                } while (mLastWallpaper.matches(papers[i].toRegex()))
            }
            mLastWallpaper = papers[i]
            return fileroot + File.separator + mLastWallpaper
        }

    init {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        DefaultWallpaper = context.filesDir.absolutePath + File.separator + "DefaultWallpaper.jpg"
        initDefaultWallpaper(context)
    }

    private fun initDefaultWallpaper(context: Context) {
        val file = File(DefaultWallpaper)
        if (file.exists())
            return
        try {
            val inputStream = context.assets.open("DefaultWallpaper.jpg")
            val outputStream = context.openFileOutput("DefaultWallpaper.jpg", Context.MODE_PRIVATE)
            val buffer = ByteArray(1024)
            var len :Int
            while (true) {
                len = inputStream.read(buffer)
                if (len == -1)
                    break
                outputStream.write(buffer, 0, len)
            }
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
