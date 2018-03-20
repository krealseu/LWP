package org.kreal.lwp.models


import java.io.File
import java.util.Random

/**
 * Created by lthee on 2017/5/27.
 * 管理壁纸文件加和获取一张壁纸
 */

class WallpaperManager(private val srcFile: File) {
    private var lastWallpaperName = ""
    fun getRandomWallpaper(): String {
        if (!srcFile.isDirectory)
            return ""
        val papers = srcFile.list { _, filename -> filename.endsWith(".jpg", true) || filename.endsWith(".png", true) }
        if (papers.isEmpty())
            return ""
        var i: Int
        if (papers.size == 1) i = 0
        else {
            do {
                i = Random().nextInt(papers.size)
            } while (lastWallpaperName == papers[i])
        }
        lastWallpaperName = papers[i]
        return srcFile.path + File.separator + lastWallpaperName
    }
}
