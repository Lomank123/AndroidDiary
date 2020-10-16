package com.lomank.diary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlinx.android.synthetic.main.activity_photo_viewer.*
import recyclerviewadapter.ImageViewPagerAdapter
import roomdatabase.Note

class PhotoViewerActivity : AppCompatActivity() {

    private var allImages = mutableListOf<String?>()

    private var allImagesOrientation = mutableListOf<Int?>()

    private lateinit var note : Note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)
        // setting toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_32)

        note = intent.getSerializableExtra("images") as Note
        val currentPos = intent.getIntExtra("currentPos", 0)

        val adapter = ImageViewPagerAdapter(this)
        image_viewPager.adapter = adapter

        if(note.images != null) {
            for(i in note.images!!.indices){
                allImages.add(note.images!![i])
                allImagesOrientation.add(note.imagesOrientation!![i])
            }

            adapter.setImages(allImages, allImagesOrientation)
            image_viewPager.setCurrentItem(currentPos, false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_image, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.rotate_left -> {
                val v = image_viewPager.findViewById<SubsamplingScaleImageView>(R.id.photoView_holder)
                rotatePicture(v.orientation + 90, v)
                allImagesOrientation[image_viewPager.currentItem] = v.orientation
            }
            R.id.rotate_right -> {
                val v = image_viewPager.findViewById<SubsamplingScaleImageView>(R.id.photoView_holder)
                rotatePicture(v.orientation - 90, v)
                allImagesOrientation[image_viewPager.currentItem] = v.orientation
            }
            R.id.delete -> {
                val dialog = MaterialDialog(this)
                val viewPager = this.findViewById<ViewPager2>(R.id.image_viewPager)
                dialog.show {
                    title(R.string.dialog_delete)
                    message(R.string.dialog_image_delete_message)
                    positiveButton(R.string.dialog_yes) {
                        allImages.removeAt(viewPager.currentItem)
                        allImagesOrientation.removeAt(viewPager.currentItem)
                        if(viewPager.currentItem == 2){
                            viewPager.setCurrentItem(1, false)
                        } else if(viewPager.currentItem == 1){
                            viewPager.setCurrentItem(0, false)
                        }
                        (viewPager.adapter as ImageViewPagerAdapter).setImages(allImages, allImagesOrientation)

                        if(allImages.isEmpty())
                            checkResultIntent()
                        dialog.dismiss()
                    }
                    negativeButton(R.string.dialog_no) {
                        dialog.dismiss()
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // rotates image on left or right side
    private fun rotatePicture(rotationInDegrees: Int, view : SubsamplingScaleImageView) {
        if(rotationInDegrees > 270){
            view.orientation = SubsamplingScaleImageView.ORIENTATION_0
        } else {
            if(rotationInDegrees == -90) {
                view.orientation = SubsamplingScaleImageView.ORIENTATION_270
            }
            else {
                view.orientation = rotationInDegrees
            }
        }
    }

    private fun checkResultIntent() {
        val resultIntent = Intent()
        note.images = allImages
        note.imagesOrientation = allImagesOrientation
        resultIntent.putExtra(EXTRA_REPLY_PHOTO_VIEWER, note)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        checkResultIntent()
    }

    companion object {
        const val EXTRA_REPLY_PHOTO_VIEWER = "EXTRA_REPLY_PHOTO_VIEWER"
    }
}