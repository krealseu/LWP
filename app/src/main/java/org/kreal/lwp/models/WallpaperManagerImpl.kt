package org.kreal.lwp.models

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import org.kreal.lwp.settings.WallpaperSource
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.regex.Pattern

class WallpaperManagerImpl(context: Context) : IWallpaperManager {
    private val srcFile: File = File(context.filesDir, WallpaperSource)

    private val pattern: Pattern = Pattern.compile(".(jpg|jpeg|png)$", Pattern.CASE_INSENSITIVE)

    private var lastWallpaper: Uri = Uri.EMPTY

    private var lastWallpaperName = ""

    private val handler = Handler(Looper.getMainLooper())

    private val executor = Executors.newSingleThreadExecutor()

    private fun doInMainThread(task: () -> Unit) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            task()
        } else
            handler.post { task() }
    }

    override fun list(callback: IWallpaperManager.LoadWallpapersCallback) = executor.execute {
        val files = srcFile.listFiles { _, s -> pattern.matcher(s).find() }
                ?: arrayOf()
        val result: SortedSet<Uri> = sortedSetOf()
        files.forEach { file ->
            result.add(Uri.fromFile(file))
        }
        doInMainThread {
            if (result.isEmpty())
                callback.onDataNotAvailable()
            else
                callback.onWallpapersLoaded(result.toList())
        }
    }

    override fun add(context: Context, uri: Uri) = executor.execute {
        if (!srcFile.isDirectory)
            return@execute
        val resolver = context.contentResolver
        val name: String = when (uri.scheme) {
            "file" -> uri.lastPathSegment
            "content" -> try {
                val c = resolver.query(uri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)
                val result = if (c.moveToFirst() && !c.isNull(0)) c.getString(0) else ""
                c.close()
                result
            } catch (e: Exception) {
                ""
            }
            else -> ""
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

    override fun delete(vararg names: Uri) = executor.execute {
        names.forEach {
            if (it.scheme == "file") {
                val file = File(it.path)
                if (file.parentFile == srcFile && file.isFile)
                    file.delete()
            }
        }
    }

    override fun getRandom(): Uri {
        if (!srcFile.isDirectory)
            return Uri.EMPTY
        val papers = srcFile.list { _, filename -> pattern.matcher(filename).find() }
        if (papers.isEmpty())
            return Uri.EMPTY
        var i: Int
        if (papers.size == 1) i = 0
        else {
            do {
                i = Random().nextInt(papers.size)
            } while (lastWallpaperName == papers[i])
        }
        lastWallpaperName = papers[i]
        return Uri.fromFile(File(srcFile.path + File.separator + lastWallpaperName))
    }

}