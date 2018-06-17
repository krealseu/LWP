package org.kreal.lwp.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.kreal.lwp.App


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (fragmentManager.findFragmentById(android.R.id.content) == null) {
            val view = MainFragment()
            fragmentManager.beginTransaction()
                    .replace(android.R.id.content, view)
                    .commit()
            MainPresenter(view, App.wallpaperManager)
        }
    }

    override fun onBackPressed() {
        if (!with(fragmentManager.findFragmentById(android.R.id.content) as MainFragment) {
                    this.onBackPressed()
                })
            super.onBackPressed()
    }
}
