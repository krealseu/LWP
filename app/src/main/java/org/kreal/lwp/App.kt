package org.kreal.lwp

import android.app.Application
import android.preference.PreferenceManager
import com.squareup.leakcanary.LeakCanary
import org.kreal.lwp.settings.WallpaperSource
import java.io.File

/**
 * Created by lthee on 2017/10/1.
 *在应用启动时绑定leak canary，初始缩放化参数
 */
class App : Application() {
    private val useLeakCanary = true
    override fun onCreate() {
        super.onCreate()
        if (useLeakCanary) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return
            }
            LeakCanary.install(this)
        }
        if (!PreferenceManager.getDefaultSharedPreferences(baseContext).getBoolean("IsInit", false)) {
            val dm = baseContext.resources.displayMetrics
            val dpi: Float = (dm.xdpi + dm.ydpi) / 6
            val scale = maxOf(dpi / dm.heightPixels, dpi / dm.widthPixels) + 1.0f
            PreferenceManager.getDefaultSharedPreferences(baseContext).edit().putFloat("PhotoFrameScale", scale)
                    .putInt("ScreenWidth", dm.widthPixels)
                    .putInt("ScreenHeight", dm.heightPixels)
                    .putBoolean("IsInit", true)
                    .apply()
        }

        val file = File(baseContext.filesDir, WallpaperSource)
        if (!file.exists())
            file.mkdirs()
        else if (file.isFile) {
            file.delete()
            file.mkdirs()
        }

    }
}