package org.kreal.lwp

import android.app.Application
import android.preference.PreferenceManager
import com.squareup.leakcanary.LeakCanary

/**
 * Created by lthee on 2017/10/1.
 */
class App : Application() {
    val USE_LEAK_CANARY = true
    override fun onCreate() {
        super.onCreate()
        if (USE_LEAK_CANARY) {
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
                    .commit()
        }


    }
}