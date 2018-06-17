package org.kreal.lwp.service

import android.content.*
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.SurfaceHolder
import android.view.WindowManager
import org.kreal.lwp.App
import org.kreal.lwp.models.PerspectiveModel
import org.kreal.lwp.models.WallpaperManager
import org.kreal.lwp.settings.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by lthee on 2017/10/3.
 * 壁纸的server，使用GLView
 */
class LWPService : GLWallpaperService() {

    override fun onCreateEngine(): Engine = SwitchEngine()

    inner class SwitchEngine : GLEngine(), GLSurfaceView.Renderer, SharedPreferences.OnSharedPreferenceChangeListener {

        private val mVMatrix = FloatArray(16)

        private val mPMatrix = FloatArray(16)

        private val perspectiveModel = PerspectiveModel(baseContext)

        private lateinit var photoFrame: PhotoFrame

        private val display = (baseContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

        private val mPerspectiveScale = PreferenceManager.getDefaultSharedPreferences(baseContext).getFloat(PhotoFrameScale, 1.0f)

        private val fpsControl: FPSControl = FPSControl(PreferenceManager.getDefaultSharedPreferences(baseContext).getString(FPSControl, "30").toInt())

        private var refreshTime: Long = (PreferenceManager.getDefaultSharedPreferences(baseContext).getString(RefreshTime, "10").toFloat() * 60000).toLong()

        private var canPerspectiveMove = PreferenceManager.getDefaultSharedPreferences(baseContext).getBoolean(CanPerspectiveMove, true)

        private var canMove: Boolean = PreferenceManager.getDefaultSharedPreferences(baseContext).getBoolean(CanMove, false)

        private var animationTime: Long = PreferenceManager.getDefaultSharedPreferences(baseContext).getString(AnimationTime, "1000").toLong()

        private var batteryLow = false

        private val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_BATTERY_LOW -> batteryLow = true
                    Intent.ACTION_POWER_CONNECTED -> batteryLow = false
                    ChangeWallpaperIntent -> changeWallpaper()
                }
            }
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            when (key) {
                RefreshTime -> refreshTime = (sharedPreferences.getString(key, "10").toFloat() * 60000).toLong()
                CanMove -> canMove = sharedPreferences.getBoolean(key, false)
                CanPerspectiveMove -> canPerspectiveMove = sharedPreferences.getBoolean(key, true)
                FPSControl -> fpsControl.setFPS(sharedPreferences.getString(key, "30").toInt())
                AnimationTime -> {
                    animationTime = sharedPreferences.getString(key, "1000").toLong()
                    photoFrame.setAnimationTime(animationTime.toFloat() / 1000)
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            PreferenceManager.getDefaultSharedPreferences(baseContext).registerOnSharedPreferenceChangeListener(this)
            setEGLContextClientVersion(2)
            setRenderer(this)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setPreserveEGLContextOnPause(true)
//            }
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)

            val batteryIntentFilter = IntentFilter()
            batteryIntentFilter.addAction(Intent.ACTION_BATTERY_LOW)
            batteryIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
            baseContext.registerReceiver(broadcastReceiver, batteryIntentFilter)
            LocalBroadcastManager.getInstance(baseContext).registerReceiver(broadcastReceiver, IntentFilter(ChangeWallpaperIntent))
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            Matrix.setLookAtM(mVMatrix, 0, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 1f, 0f)
            photoFrame = PhotoFrame()
            photoFrame.setAnimationTime(animationTime.toFloat() / 1000)
            photoFrame.setSrc(App.wallpaperManager.getRandom().path)
            MatrixState.setCamera(mVMatrix)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            val radio = height.toFloat() / width.toFloat()
            photoFrame.setSize(2f * mPerspectiveScale, 2f * radio * mPerspectiveScale)
            Matrix.orthoM(mPMatrix, 0, -1.0f, 1.0f, -radio, radio, 1f, 3f)
            MatrixState.setProjMatrix(mPMatrix)
        }

        override fun onDrawFrame(gl: GL10?) {
            MatrixState.setInitStack()
            if (canPerspectiveMove) {
                val perspectivePos: FloatArray = perspectiveModel.getValue(display.rotation)
                MatrixState.translate(perspectivePos[1] * (mPerspectiveScale - 1) / 2, -perspectivePos[0] * (mPerspectiveScale - 1) / 2, 0f)
            }
            photoFrame.draw(MatrixState.getFinalMatrix())
            fpsControl.asyncWait {
                if (canPerspectiveMove || photoFrame.isTransition())
                    requestRender()
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                if (canPerspectiveMove && !batteryLow) {
                    perspectiveModel.enable()
                }
                if (isNeedRefresh()) {
                    changeWallpaper()
                }
                requestRender()
            } else {
                perspectiveModel.disable()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            PreferenceManager.getDefaultSharedPreferences(baseContext).unregisterOnSharedPreferenceChangeListener(this)
            baseContext.unregisterReceiver(broadcastReceiver)
            LocalBroadcastManager.getInstance(baseContext).unregisterReceiver(broadcastReceiver)
        }

        private fun changeWallpaper() {
            queueEvent(Runnable {
                if (isVisible) {
                    val name = App.wallpaperManager.getRandom().path
                    lastRefreshTime = System.currentTimeMillis()
                    photoFrame.setSrc(name)
                    requestRender()
                }
            })
        }

        private var lastRefreshTime: Long = System.currentTimeMillis()

        private fun isNeedRefresh(): Boolean = Math.abs(System.currentTimeMillis() - lastRefreshTime) > refreshTime

    }

    companion object {
        const val ChangeWallpaperIntent = "org.kreal.lwp.action.CHANGE_WALLPAPER"
    }

    class FPSControl(fps: Int) {

        private val nanosPERMilli = 1000000

        private val nanosPERSecond = 1000000000

        private var frameDuration: Long = (nanosPERSecond / fps).toLong()

        private var sleepTime: Long = 0L

        private var lastRefreshTime: Long = 0L

        fun setFPS(fps: Int) {
            frameDuration = (nanosPERSecond / fps).toLong()
        }

        fun asyncWait(request: () -> Unit) {
            val showTime = System.nanoTime()
            Thread {
                sleepTime -= (showTime - lastRefreshTime - frameDuration)
                if (sleepTime <= 0) {
                    sleepTime = 0
                } else {
                    Thread.sleep(sleepTime / nanosPERMilli, (sleepTime % nanosPERMilli).toInt())
                }
                lastRefreshTime = showTime
                request()
            }.start()
        }

        fun blockWait() {
            val showTime = System.nanoTime()
            sleepTime -= (showTime - lastRefreshTime - frameDuration)
            if (sleepTime <= 0) {
                sleepTime = 0
            } else {
                Thread.sleep(sleepTime / nanosPERMilli, (sleepTime % nanosPERMilli).toInt())
            }
            lastRefreshTime = showTime
        }
    }
}