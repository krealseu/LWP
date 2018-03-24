package org.kreal.lwp.backup

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.preference.PreferenceManager
import android.widget.Toast
import org.kreal.lwp.settings.*
import java.io.*
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Created by lthee on 2018/3/23.
 * backup将app的壁纸和用户preference打包层zip文件，并放入大Download文件下
 * restore从Download文件下读取指定文件，并加载壁纸和preference
 */
class BackupManager(private val context: Context) {
    private val wallpaperSource: File = context.getFileStreamPath(WallpaperSource)

    private val configureFileName = "lwp.configure"

    private val splitMark = '\t'

    private val configureFile: File = context.getFileStreamPath(configureFileName)

    private val pattern: Pattern = Pattern.compile(".(jpg|jpeg|png)$", Pattern.CASE_INSENSITIVE)

    fun backup(name: String = "lwp.zip") {
        saveConfigure()
        val outputFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name)
        val zos = ZipOutputStream(FileOutputStream(outputFile))
        val byteArray = ByteArray(1024 * 100)
        zos.putNextEntry(ZipEntry("$WallpaperSource/"))
        zos.closeEntry()
        wallpaperSource.listFiles().forEach {
            if (it.isFile && pattern.matcher(it.name).find()) {
                zos.putNextEntry(ZipEntry("$WallpaperSource/${it.name}"))
                val inputStream = FileInputStream(it)
                copyStream(inputStream, zos, byteArray)
                inputStream.close()
                zos.flush()
                zos.closeEntry()
            }
        }
        zos.putNextEntry(ZipEntry(configureFile.name))
        val inputStream = FileInputStream(configureFile)
        copyStream(inputStream, zos, byteArray)
        inputStream.close()
        zos.flush()
        zos.closeEntry()
        zos.finish()
    }

    fun restore(name: String = "lwp.zip") {
        val backupFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name)
        if (!backupFile.isFile) {
            Toast.makeText(context, "Fail:File $name is't exists", Toast.LENGTH_SHORT).show()
            return
        }
        val zis = ZipInputStream(FileInputStream(backupFile))
        while (true) {
            val entry = zis.nextEntry ?: break
            val byteArray = ByteArray(1024 * 100)
            if (pattern.matcher(entry.name).find()) {
                val file = File(wallpaperSource, entry.name.substring(entry.name.lastIndexOf('/')))
                if (file.exists() && file.isDirectory)
                    file.delete()
                val fileOutputStream = FileOutputStream(file)
                copyStream(zis, fileOutputStream, byteArray)
                fileOutputStream.flush()
                fileOutputStream.close()
            } else if (entry.name == configureFileName) {
                val fileOutputStream = FileOutputStream(configureFile)
                copyStream(zis, fileOutputStream, byteArray)
                fileOutputStream.flush()
                fileOutputStream.close()
            }
            zis.closeEntry()
        }
        zis.close()
        loadConfigure()
    }

    private fun saveConfigure() {
        if (configureFile.exists())
            configureFile.delete()
        configureFile.createNewFile()
        val bufferedWriter = BufferedWriter(FileWriter(configureFile))
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        writeStringPrfToBuffer(preference, FPSControl, bufferedWriter)
        writeStringPrfToBuffer(preference, WallpaperSource, bufferedWriter)
        writeStringPrfToBuffer(preference, AnimationTime, bufferedWriter)
        writeStringPrfToBuffer(preference, RefreshTime, bufferedWriter)
        writeBoolPrfToBuffer(preference, CanMove, bufferedWriter)
        writeBoolPrfToBuffer(preference, CanPerspectiveMove, bufferedWriter)
        bufferedWriter.close()
    }

    private fun loadConfigure() {
        if (configureFile.exists() && configureFile.isFile) {
            val bufferedReader = BufferedReader(FileReader(configureFile))
            val preference = PreferenceManager.getDefaultSharedPreferences(context)
            while (true) {
                val line = bufferedReader.readLine() ?: break
                val strings = line.split(splitMark)
                if (strings.size == 2) {
                    when (strings[0]) {
                        FPSControl, WallpaperSource, AnimationTime, RefreshTime -> preference.edit().putString(strings[0], strings[1]).apply()
                        CanMove, CanPerspectiveMove -> preference.edit().putBoolean(strings[0], strings[1].toBoolean()).apply()
                    }
                }
            }
            bufferedReader.close()
        }
    }

    private fun writeStringPrfToBuffer(preference: SharedPreferences, key: String, bufferedWriter: BufferedWriter) {
        if (preference.contains(key)) {
            bufferedWriter.write("$key$splitMark${preference.getString(key, "")}")
            bufferedWriter.newLine()
        }
    }

    private fun writeBoolPrfToBuffer(preference: SharedPreferences, key: String, bufferedWriter: BufferedWriter) {
        if (preference.contains(key)) {
            bufferedWriter.write("$key$splitMark${preference.getBoolean(key, false)}")
            bufferedWriter.newLine()
        }
    }

    private fun copyStream(srcStream: InputStream, tagStream: OutputStream, byteArray: ByteArray) {
        while (true) {
            val len = srcStream.read(byteArray)
            if (len == -1)
                break
            tagStream.write(byteArray, 0, len)
        }
    }
}