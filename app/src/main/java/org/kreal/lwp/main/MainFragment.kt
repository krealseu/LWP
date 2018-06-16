package org.kreal.lwp.main

import android.app.Activity
import android.app.Fragment
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import org.kreal.lwp.R
import org.kreal.lwp.settings.SettingsActivity
import java.io.File

class MainFragment : Fragment(), MainContract.View, View.OnClickListener {

    private val requestImageCode = 21
    private val selectImages: MutableList<Uri> = arrayListOf()
    private lateinit var adapter: ImageAdapter

    private lateinit var presenter: MainContract.Presenter
    private lateinit var mAdapter: ImageAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var selectBar: FrameLayout
    private lateinit var selectBarCancel: ImageView
    private lateinit var selectBarDelete: ImageView
    private lateinit var selectBarInfo: TextView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater?.inflate(R.layout.activity_main, container, false)!!
        setHasOptionsMenu(true)
        retainInstance = true
        (activity as AppCompatActivity).setSupportActionBar(view.findViewById(R.id.toolbar))
        // init var
        with(view) {
            fab = findViewById(R.id.fab)
            selectBar = findViewById(R.id.select_bar)
            selectBarDelete = findViewById(R.id.select_bar_delete)
            selectBarCancel = findViewById(R.id.select_bar_cancel)
            selectBarInfo = findViewById(R.id.select_bar_info)
            with(findViewById<RecyclerView>(R.id.wallpaperRecycleView)) {
                val display = resources.displayMetrics
                val num = if (display.widthPixels >= 1200) display.widthPixels / 400 else 3
                this@MainFragment.adapter = ImageAdapter(emptyList(), display.heightPixels / num)
                adapter = this@MainFragment.adapter
                layoutManager = GridLayoutManager(context, num)
            }
        }


        fab.setOnClickListener(this)
        selectBarCancel.setOnClickListener(this)
        selectBarDelete.setOnClickListener(this)
        return view
    }

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            when (requestCode) {
                requestImageCode -> presenter.addPaper(activity, data?.data!!)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            R.id.action_live_wallpaper -> {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
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

    /**
     * 添加一张壁纸
     */
    private fun actionAdd() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val annotation = RotateAnimation(0f, 90f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        annotation.duration = 200
        fab.startAnimation(annotation)
        startActivityForResult(intent, requestImageCode)
    }

    /**
     * 删除选中的壁纸
     */
    private fun actionDelete() {
        setSelectViewVisiable(false)
        presenter.deletePaper(*selectImages.toTypedArray())
        selectImages.clear()
    }

    /**
     * 取消选择
     */
    private fun actionCancel() {
        setSelectViewVisiable(false)
        selectImages.clear()

    }

    /**
     * 展示点击的图片
     */
    private fun actionShow(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        val data = FileProvider.getUriForFile(activity, "org.kreal.lwp", File(uri.path))
        intent.setDataAndType(data, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    private fun setSelectViewVisiable(visible: Boolean) {
        if (visible) {
            selectBar.visibility = View.VISIBLE
            selectBarInfo.text = String.format("Selected %d", selectImages.size)
            fab.visibility = View.INVISIBLE
        } else {
            selectBar.visibility = View.INVISIBLE
            fab.visibility = View.VISIBLE
        }
        val animation = if (visible) ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f) else AlphaAnimation(1f, 0f)
        animation.duration = 200
        val annotation2 = if (visible) ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f) else ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        annotation2.duration = 200
        fab.startAnimation(annotation2)
        selectBar.startAnimation(animation)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.fab -> actionAdd()
            R.id.select_bar_cancel -> actionCancel()
            R.id.select_bar_delete -> actionDelete()
        }
    }

    override fun setPresenter(presenter: MainContract.Presenter) {
        this.presenter = presenter
    }

    override fun showLoading() {
    }

    override fun showData(data: List<Uri>) {
        adapter.swapData(data)
    }

    inner class ImageAdapter(private var data: List<Uri>, private val imageHeight: Int) : RecyclerView.Adapter<ImageAdapter.ItemHolder>() {

        fun swapData(data: List<Uri>) {
            this.data = data
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemHolder {
            val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_image, parent, false)
            val itemHolder = ItemHolder(view)
            itemHolder.apply {
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.minimumHeight = imageHeight
                itemView.setOnClickListener {
                }
                itemView.setOnLongClickListener {
                    true
                }

            }
            return itemHolder
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: ItemHolder?, position: Int) {
            holder?.apply {
                Glide.with(holder.itemView).load(data[adapterPosition]).into(imageView)
                shadow.visibility = if (selectImages.contains(data[position])) View.VISIBLE else View.INVISIBLE
            }
        }

        inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.item_image)
            val shadow: View = itemView.findViewById(R.id.item_shadow)
            fun showOrHideShadow(visible: Boolean) {
                shadow.visibility = if (visible) View.VISIBLE else View.INVISIBLE
                val annotation = if (visible) AlphaAnimation(0f, 1f) else AlphaAnimation(1f, 0f)
                annotation.duration = 200
                shadow.startAnimation(annotation)
            }
        }

    }
}