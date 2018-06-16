package org.kreal.lwp.models

import android.content.Context
import android.net.Uri
import org.kreal.lwp.settings.WallpaperSource
import java.io.File
import java.util.regex.Pattern

class WallpaperManagerImpl(context: Context) : IWallpaerManager {
    private val srcFile: File = File(context.filesDir, WallpaperSource)

    private val pattern: Pattern = Pattern.compile(".(jpg|jpeg|png)$", Pattern.CASE_INSENSITIVE)

    override fun list(): List<Uri> {
        val files = srcFile.listFiles { _, s -> pattern.matcher(s).find() }
                ?: arrayOf()
        val result = arrayListOf<Uri>()
        files.forEach { file ->
            result.add(Uri.fromFile(file))
        }
        return result
    }

    override fun add(context: Context, uri: Uri) {
    }

    override fun delete(vararg names: Uri) {
    }

    override fun getRandom(): Uri {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setChangeListener(listener: IWallpaerManager.Callback) {
    }
}