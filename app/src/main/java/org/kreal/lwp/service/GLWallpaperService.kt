package org.kreal.lwp.service

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * Created by lthee on 2017/5/3.
 */

abstract class GLWallpaperService : WallpaperService() {
    open inner class GLEngine : WallpaperService.Engine() {
        private val mGLSurfaceView = GLEngineView(applicationContext)

        internal inner class GLEngineView(context: Context) : GLSurfaceView(context) {

            override fun getHolder(): SurfaceHolder {
                return surfaceHolder
            }

            public override fun onDetachedFromWindow() {
                super.onDetachedFromWindow()
            }
        }

        fun setRenderer(render: Renderer) {
            mGLSurfaceView.setRenderer(render)
        }

        fun setEGLContextClientVersion(version: Int) {
            mGLSurfaceView.setEGLContextClientVersion(version)
        }

        fun setRenderMode(renderMode: Int) {
            mGLSurfaceView.renderMode = renderMode
        }

        fun requestRender() {
            mGLSurfaceView.requestRender()
        }

        fun setPreserveEGLContextOnPause(b: Boolean) {
            mGLSurfaceView.preserveEGLContextOnPause = b
        }

        fun queueEvent(r: Runnable) {
            mGLSurfaceView.queueEvent(r)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                mGLSurfaceView.onResume()
            } else
                mGLSurfaceView.onPause()
        }

        override fun onDestroy() {
            super.onDestroy()
            mGLSurfaceView.onDetachedFromWindow()
        }
    }
}
