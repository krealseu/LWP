package org.kreal.lwp

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.Loader
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.content_main.*
import org.kreal.lwp.adapters.ImageAdapter
import org.kreal.lwp.adapters.ImageAdapterData
import org.kreal.lwp.loader.WallpaperLoader
import org.kreal.lwp.models.WallpaperManager
import org.kreal.lwp.settings.SettingsActivity
import org.kreal.lwp.settings.WallpaperSource
import java.io.File
import java.util.ArrayList


class MainActivity : AppCompatActivity(), android.app.LoaderManager.LoaderCallbacks<List<Uri>>,
        View.OnClickListener, ImageAdapter.OnItemClickListener, ImageAdapter.OnItemLongClickListener {
    override fun onItemLongClick(itemHolder: ImageAdapter.ItemHolder, data: Uri, position: Int): Boolean {
        if (mAdapterData.selects.isEmpty()) {
            mAdapterData.setDataState(data, true)
            showOrHideSelectView(true)
            mAdapter.notifyDataSetChanged()
        } else {
            actionCancel()
        }
        return true
    }

    override fun onItemClick(itemHolder: ImageAdapter.ItemHolder, data: Uri, position: Int) {
        if (mAdapterData.selects.isEmpty())
            actionShow(data)
        else {
            val state = mAdapterData.getDataState(data)
            val select = mAdapterData.selects
            mAdapterData.setDataState(data, !state)
            itemHolder.showOrHideShadow(!state)
            selectBarInfo.text = String.format("Selected %d", select.size)
            if ((selectBar.visibility == View.VISIBLE) == select.isEmpty())
                showOrHideSelectView(false)
            if ((selectBar.visibility != View.VISIBLE) == (!select.isEmpty()))
                showOrHideSelectView(true)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> {
                when (view.tag) {
                    fabTagDelete -> actionDelete()
                    else -> actionAdd()
                }
            }
            R.id.select_bar_cancel -> actionCancel()
            R.id.select_bar_delete -> actionDelete()
        }
    }

    private fun actionCancel() {
        showOrHideSelectView(false)
        mAdapterData.selects.clear()
        mAdapter.notifyDataSetChanged()
    }

    private fun actionDelete() {
        mAdapterData.selects.forEach {
            wallpaperManager.delete(it.path.substring(it.path.lastIndexOf('/')))
        }
        showOrHideSelectView(false)
        mAdapterData.selects.clear()
    }

    private fun actionAdd() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val annotation = RotateAnimation(0f, 90f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        annotation.duration = 200
        fab.startAnimation(annotation)
        startActivityForResult(intent, requestImageCode)
    }

    private fun actionShow(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        val data = FileProvider.getUriForFile(baseContext, "org.kreal.lwp", File(uri.path))
        intent.setDataAndType(data, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    private fun showOrHideSelectView(visible: Boolean) {
        if (visible) {
            selectBar.visibility = View.VISIBLE
            selectBarInfo.text = String.format("Selected %d", mAdapterData.selects.size)
            fab.tag = fabTagDelete
            fab.setImageResource(R.drawable.ic_fab_delete)
        } else {
            selectBar.visibility = View.INVISIBLE
            fab.tag = fabTagAdd
            fab.setImageResource(R.drawable.ic_fab_add)
        }
        val animation = if (visible) ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f) else AlphaAnimation(1f, 0f)
        animation.duration = 200
        val annotation2 = AlphaAnimation(0.8f, 1f)
        annotation2.duration = 200
        fab.startAnimation(annotation2)
        selectBar.startAnimation(animation)
    }

    override fun onLoaderReset(p0: Loader<List<Uri>>?) {
        mAdapterData.data = arrayListOf()
        mAdapter.notifyDataSetChanged()
    }

    override fun onLoadFinished(p0: Loader<List<Uri>>?, data: List<Uri>) {
        mAdapterData.data = data
        mAdapter.notifyDataSetChanged()
    }

    override fun onCreateLoader(p0: Int, p1: Bundle?): Loader<List<Uri>> = WallpaperLoader(baseContext)

    private val loaderID: Int = 233
    private val requestImageCode = 21
    private val fabTagAdd = "add"
    private val fabTagDelete = "delete"

    private val mAdapterData: ImageAdapterData = ImageAdapterData(arrayListOf())
    private lateinit var mAdapter: ImageAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var selectBar: FrameLayout
    private lateinit var selectBarCancel: ImageView
    private lateinit var selectBarDelete: ImageView
    private lateinit var selectBarInfo: TextView
    private lateinit var wallpaperManager: WallpaperManager


    private lateinit var gridlayoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //restore data
        savedInstanceState?.also {
            mAdapterData.selects.addAll(it.getParcelableArrayList("SELECT"))
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val display = resources.displayMetrics
        val num = if (display.widthPixels >= 1200) display.widthPixels / 400 else 3

        // init var
        fab = findViewById(R.id.fab)
        selectBar = findViewById(R.id.select_bar)
        selectBarDelete = findViewById(R.id.select_bar_delete)
        selectBarCancel = findViewById(R.id.select_bar_cancel)
        selectBarInfo = findViewById(R.id.select_bar_info)
        mAdapter = ImageAdapter(mAdapterData, display.heightPixels / num)
        gridlayoutManager = GridLayoutManager(baseContext, num)
        wallpaperManager = WallpaperManager(File(baseContext.filesDir, WallpaperSource))

        fab.setOnClickListener(this)
        selectBarCancel.setOnClickListener(this)
        selectBarDelete.setOnClickListener(this)
        mAdapter.setOnItemClickListener(this)
        mAdapter.setOnItemLongClickListener(this)
        wallpaperRecycleView.layoutManager = gridlayoutManager
        wallpaperRecycleView.adapter = mAdapter

        if (!mAdapterData.selects.isEmpty())
            showOrHideSelectView(true)

        loaderManager.initLoader(loaderID, null, this)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                requestImageCode -> wallpaperManager.add(baseContext, data?.data!!)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(baseContext, SettingsActivity::class.java))
                return true
            }
            R.id.action_live_wallpaper -> {
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

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelableArrayList("SELECT", mAdapterData.selects as ArrayList<out Parcelable>)
    }

    override fun onBackPressed() {
        if (mAdapterData.selects.isEmpty())
            super.onBackPressed()
        else actionCancel()
    }

}
