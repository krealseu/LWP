package org.kreal.lwp.models


import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern

/**
 * Created by lthee on 2017/5/27.
 * 管理壁纸文件加和获取一张壁纸
 */

class WallpaperManager(private val srcFile: File) {
    private var lastWallpaperName = ""

    private val pattern: Pattern = Pattern.compile(".(jpg|jpeg|png)$", Pattern.CASE_INSENSITIVE)

    fun getRandomWallpaper(): String {
        if (!srcFile.isDirectory)
            return ""
        val papers = srcFile.list { _, filename -> pattern.matcher(filename).find() }
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

    fun add(file: File) {
        if (file.isFile) {
            if (pattern.matcher(file.name).find()) {
                val targetFile = File(srcFile, file.name)
                val outputChannel = FileOutputStream(targetFile).channel
                val inputChannel = FileInputStream(file).channel
                inputChannel.transferTo(0, inputChannel.size(), outputChannel)
                outputChannel.close()
                inputChannel.close()
            }
        }
    }

    fun add(name: String, inputStream: InputStream) {
        if (pattern.matcher(name).find()) {
            val targetFile = File(srcFile, name)
            if (targetFile.exists())
                targetFile.delete()
            val outputStream = FileOutputStream(targetFile)
            val byteArray = ByteArray(1024)
            var len: Int
            while (true) {
                len = inputStream.read(byteArray)
                if (len == -1)
                    break
                outputStream.write(byteArray, 0, len)
            }
            outputStream.close()
        }
    }

    fun add(context: Context, uri: Uri) {
        if (!srcFile.isDirectory)
            return
        val resolver = context.contentResolver
        val name: String = try {
            val c = resolver.query(uri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)
            val result = if (c.moveToFirst() && !c.isNull(0)) c.getString(0) else ""
            c.close()
            result
        } catch (e: Exception) {
            ""
        }
        if (pattern.matcher(name).find()) {
            val targetFile = File(srcFile, name)
            if (targetFile.exists())
                targetFile.delete()
            val outputChannel = FileOutputStream(targetFile).channel
            val fileDescriptor = resolver.openFileDescriptor(uri, "r")
            val inputChannel = FileInputStream(fileDescriptor.fileDescriptor).channel
            inputChannel.transferTo(0, inputChannel.size(), outputChannel)
            outputChannel.close()
            inputChannel.close()
            fileDescriptor.close()
        }
    }

    fun delete(name: String) {
        val file = File(srcFile, name)
        if (file.isFile)
            file.delete()
    }

}
