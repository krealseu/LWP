package org.kreal.lwp.service

import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

/**
 * Created by lthee on 2017/5/3.
 * Wallpaper Service Engine 行为改为向GLSurfaceView
 */

abstract class GLWallpaperService : WallpaperService() {
    open inner class GLEngine : WallpaperService.Engine() {

        private val mGLSurfaceView = object : GLSurfaceView(baseContext) {
            //将GLSurfaceView的要使用的surfaceHolder重载替换为Engine的SurfaceHolder
            override fun getHolder(): SurfaceHolder = surfaceHolder

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

        fun setPreserveEGLContextOnPause(boolean: Boolean) {
            mGLSurfaceView.preserveEGLContextOnPause = boolean
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
