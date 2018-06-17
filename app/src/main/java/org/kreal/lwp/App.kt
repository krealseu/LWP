package org.kreal.lwp

import android.app.Application
import android.preference.PreferenceManager
import org.kreal.lwp.models.IWallpaperManager
import org.kreal.lwp.models.WallpaperManagerImpl
import org.kreal.lwp.settings.PhotoFrameScale
import org.kreal.lwp.settings.WallpaperSource

/**
 * Created by lthee on 2017/10/1.
 *在应用启动时绑定leak canary，初始缩放化参数
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        wallpaperManager = WallpaperManagerImpl(applicationContext)

        if (!PreferenceManager.getDefaultSharedPreferences(baseContext).getBoolean("IsInit", false)) {
            val file = getFileStreamPath(WallpaperSource)
            if (!file.isDirectory) {
                file.delete()
                file.mkdirs()
            }
            val dm = baseContext.resources.displayMetrics
            val dpi: Float = (dm.xdpi + dm.ydpi) / 6
            val scale = maxOf(dpi / dm.heightPixels, dpi / dm.widthPixels) + 1.0f
            PreferenceManager.getDefaultSharedPreferences(baseContext).edit().putFloat(PhotoFrameScale, scale)
                    .putInt("ScreenWidth", dm.widthPixels)
                    .putInt("ScreenHeight", dm.heightPixels)
                    .putBoolean("IsInit", true)
                    .apply()
        }
    }

    companion object {
        lateinit var wallpaperManager: IWallpaperManager
            private set
    }
}