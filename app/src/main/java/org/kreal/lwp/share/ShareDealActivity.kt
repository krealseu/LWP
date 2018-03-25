package org.kreal.lwp.share

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import org.kreal.lwp.R
import org.kreal.lwp.adapters.ImageAdapter
import org.kreal.lwp.adapters.ImageAdapterData

class ShareDealActivity : AppCompatActivity(), ImageAdapter.OnItemClickListener {
    override fun onItemClick(itemHolder: ImageAdapter.ItemHolder, data: Uri, position: Int) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(data, "image/*")
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val action = intent.action
        val type = intent.type
        if (action == Intent.ACTION_SEND) {
            if (type.startsWith("image/"))
                dealPicStream(intent.getParcelableExtra(Intent.EXTRA_STREAM))
            else if (type == "application/zip")
                dealZipStream(intent.getParcelableExtra(Intent.EXTRA_STREAM))
        } else if (action == Intent.ACTION_SEND_MULTIPLE && type.startsWith("image/")) {
            dealMultiplePicStream(intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM))
        } else showError()

    }

    private fun dealZipStream(uri: Uri) {
        val frameLayout = FrameLayout(baseContext)
        val textView = TextView(baseContext)
        textView.setText(R.string.load_from_zip)
        textView.setTextColor(Color.BLACK)
        textView.textSize = 30f
        textView.gravity = Gravity.CENTER
        frameLayout.addView(textView)
        val button = Button(baseContext)
        val buttonLayout = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        buttonLayout.gravity = Gravity.BOTTOM or Gravity.CENTER
        button.setText(R.string.load)
        button.background.alpha = 160
        button.setOnClickListener {
            ShareDealService.startAction(baseContext, arrayListOf(uri), ShareDealService.Zip)
            finish()
        }
        frameLayout.addView(button, buttonLayout)
        setContentView(frameLayout)
    }

    private fun dealPicStream(uri: Uri) {
        val frameLayout = FrameLayout(baseContext)
        val imageView = ImageView(baseContext)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(baseContext).load(uri).into(imageView)
        frameLayout.addView(imageView)
        val button = Button(baseContext)
        val buttonLayout = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        buttonLayout.gravity = Gravity.BOTTOM or Gravity.CENTER
        button.setText(R.string.save)
        button.background.alpha = 160
        button.setOnClickListener {
            ShareDealService.startAction(baseContext, arrayListOf(uri), ShareDealService.Image)
            finish()
        }
        frameLayout.addView(button, buttonLayout)
        setContentView(frameLayout)
    }

    private fun dealMultiplePicStream(arrayList: ArrayList<Uri>) {
        val frameLayout = FrameLayout(baseContext)
        val recyclerView = RecyclerView(baseContext)
        val button = Button(baseContext)
        frameLayout.addView(recyclerView)
        val buttonLayout = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        buttonLayout.gravity = Gravity.BOTTOM or Gravity.CENTER
        frameLayout.addView(button, buttonLayout)
        setContentView(frameLayout)

        val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        val num = if (point.x >= 1000) point.x / 500 else 2
        val adapter = ImageAdapter(ImageAdapterData(arrayList), (point.y / num))
        recyclerView.layoutManager = GridLayoutManager(baseContext, num)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(this)
        button.setText(R.string.save)
        button.background.alpha = 160
        button.setOnClickListener {
            ShareDealService.startAction(baseContext, arrayList, ShareDealService.Image)
            finish()
        }
    }

    private fun showError() {
        val textView = TextView(baseContext)
        textView.setText(R.string.error_share)
        textView.setTextColor(Color.RED)
        textView.textSize = 30f
        textView.gravity = Gravity.CENTER
        setContentView(textView)
    }
}
