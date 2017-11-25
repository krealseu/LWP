package org.kreal.lwp

import android.content.ComponentName
import android.content.Intent
import android.content.Loader
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import kotlinx.android.synthetic.main.content_main.*
import org.kreal.lwp.SQLDatabase.WallpaperManager
import org.kreal.lwp.adapters.ImageAdapter
import org.kreal.lwp.loader.WallpaperLoader
import org.kreal.lwp.settings.SettingsActivity

class MainActivity : AppCompatActivity(), android.app.LoaderManager.LoaderCallbacks<Array<String>> {
    override fun onLoaderReset(p0: Loader<Array<String>>?) {
        mAdapter.swapeData(null)
    }

    override fun onLoadFinished(p0: Loader<Array<String>>?, p1: Array<String>?) {
        mAdapter.swapeData(p1)
    }

    override fun onCreateLoader(p0: Int, p1: Bundle?): Loader<Array<String>> {
        return WallpaperLoader(baseContext)
    }

    lateinit var mAdapter: ImageAdapter
    lateinit var wallmanager: WallpaperManager


    private lateinit var gridlayoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        loaderManager.initLoader(0, null, this)
        mAdapter = ImageAdapter(baseContext, null)
        gridlayoutManager = GridLayoutManager(baseContext, 2)
        wallpaperRecycleView.layoutManager = gridlayoutManager
        wallpaperRecycleView.adapter = mAdapter
        wallmanager = WallpaperManager(baseContext)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

//        wallpaperRecycleView.layoutManager=gridlayoutManager

        return when (id) {
            R.id.action_settings -> {
                startActivity(Intent(baseContext, SettingsActivity::class.java))
                return true
            }
            R.id.action_startlwp -> {
                val intent = Intent(android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                val componentName = ComponentName("org.kreal.lwp", "org.kreal.lwp.service.LWPService")
                intent.putExtra(android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName)
                startActivity(intent)
                return true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}
