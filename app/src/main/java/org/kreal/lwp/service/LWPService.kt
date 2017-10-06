package org.kreal.lwp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Build
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.view.SurfaceHolder
import android.view.WindowManager
import org.kreal.lwp.models.FileManager
import org.kreal.lwp.models.PerspectiveModle
import org.kreal.lwp.service.imageview.PhotoContainer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by lthee on 2017/10/3.
 */
class LWPService : GLWallpaperService() {
    var batteryLow = false
    val batteyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_LOW -> batteryLow = true
                Intent.ACTION_POWER_CONNECTED -> batteryLow = false
            }
        }
    }

    override fun onCreateEngine(): Engine {
        var intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW)
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        baseContext.registerReceiver(batteyReceiver, intentFilter)
        return SwitchEngice()
    }

    override fun onDestroy() {
        super.onDestroy()
        baseContext.unregisterReceiver(batteyReceiver)
    }

    inner class SwitchEngice : GLEngine(), GLSurfaceView.Renderer {
        val wallpapers = FileManager(baseContext)

        private val mVMatriix = FloatArray(16)
        private val mPMatrix = FloatArray(16)
        private var mPerspectiveScale = PreferenceManager.getDefaultSharedPreferences(baseContext).getFloat("PhotoFrameScale", 1.0f)

        private var visible = false
        private var perspectiveMoveAble = true

        private var perspectiveModle = PerspectiveModle(baseContext)

        private lateinit var photoFrame: PhotoContainer

        var display = (applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            setEGLContextClientVersion(2)
            setRenderer(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                setPreserveEGLContextOnPause(true)
            }
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)
        }


        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            val radio = height.toFloat() / width.toFloat()
            photoFrame.setSize(2f * mPerspectiveScale, 2f * radio * mPerspectiveScale)
            Matrix.orthoM(mPMatrix, 0, -1.0f, 1.0f, -radio, radio, 1f, 3f)
            MatrixState.setProjMatrix(mPMatrix)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            Matrix.setLookAtM(mVMatriix, 0, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 1f, 0f)
            photoFrame = PhotoFrame()
            photoFrame.serSrc(wallpapers.randomWallpaper)
            MatrixState.setCamera(mVMatriix)
        }

        private var isDrawing = false

        override fun onDrawFrame(gl: GL10?) {
            limitFrameRate()
            isDrawing = true
            MatrixState.setInitStack()
            var prespectivePos = FloatArray(2)
            val screenRotation = display.rotation
            if (perspectiveMoveAble)
                prespectivePos = perspectiveModle.getValue(screenRotation)

            MatrixState.translate(prespectivePos[1] * (mPerspectiveScale - 1) / 2, -prespectivePos[0] * (mPerspectiveScale - 1) / 2, 0f)

            photoFrame.draw(MatrixState.getFinalMatrix())

            isDrawing = false
            requestRender()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            this.visible = visible
            if (visible) {
                if (perspectiveMoveAble && !batteryLow) {
                    perspectiveModle.enable()
                }
                if (isNeedRefresh()) {
                    changeWallpaper()
                }
                requestRender()
            } else {
                perspectiveModle.disable()
            }
        }

        var animationStartTime = SystemClock.elapsedRealtime()
        private fun changeWallpaper() {
            queueEvent(Runnable {
                val name = wallpapers.randomWallpaper
                photoFrame.serSrc(name)
                animationStartTime = SystemClock.elapsedRealtime()
                requestRender()
            })
        }

        private var lastRefreshThime: Long = System.currentTimeMillis()
        private fun isNeedRefresh(): Boolean {
            val time = System.currentTimeMillis()
            val result: Boolean
            if (Math.abs(time - lastRefreshThime) > 300) {
                lastRefreshThime = time
                result = true
            } else
                result = false
            return result
        }

        private val FPS = (1000 / 30).toLong()
        private var frameStartTimeMs: Long = 0
        private fun limitFrameRate() {
            val elapasedFrameTimes = SystemClock.elapsedRealtime() - frameStartTimeMs
            val timeToSleepMs = FPS - elapasedFrameTimes
            if (timeToSleepMs > 0)
                SystemClock.sleep(timeToSleepMs)
            frameStartTimeMs = SystemClock.elapsedRealtime()
        }

    }

}