package com.lomank.diary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlinx.android.synthetic.main.activity_photo_viewer.*
import recyclerviewadapter.ImageViewPagerAdapter
import roomdatabase.Note


class PhotoViewerActivity : AppCompatActivity() {

    private var allImages = mutableListOf<String?>()

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

        //TODO: Возможно нужно передавать в адаптер всю заметку, а в заметке сделать поле с массивом размеров
        // Эти размеры будут использоваться для отображения фото с правильной ориентацией
        val adapter = ImageViewPagerAdapter(this)
        image_viewPager.adapter = adapter
        if(note.images != null) {

            for(image in note.images!!)
                allImages.add(image)
            adapter.setImages(allImages)
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
            }
            R.id.rotate_right -> {
                val v = image_viewPager.findViewById<SubsamplingScaleImageView>(R.id.photoView_holder)
                rotatePicture(v.orientation - 90, v)
            }
            R.id.delete -> {
                // TODO: Implement dialog screen
                allImages.removeAt(image_viewPager.currentItem)
                if(image_viewPager.currentItem == 2){
                    image_viewPager.setCurrentItem(1, false)
                } else if(image_viewPager.currentItem == 1){
                    image_viewPager.setCurrentItem(0, false)
                }
                (image_viewPager.adapter as ImageViewPagerAdapter).setImages(allImages)

                if(allImages.isEmpty())
                    checkResultIntent()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // rotates image on left or right side
    private fun rotatePicture(rotationInDegrees: Int, view : SubsamplingScaleImageView) {
        if(rotationInDegrees > 270){
            view.orientation = SubsamplingScaleImageView.ORIENTATION_0
        } else {
            if(rotationInDegrees == -90)
                view.orientation = SubsamplingScaleImageView.ORIENTATION_270
            else
                view.orientation = rotationInDegrees
        }
    }

    private fun checkResultIntent() {
        val resultIntent = Intent()
        if(note.images!! != allImages) {
            note.images = allImages
            resultIntent.putExtra(EXTRA_REPLY_PHOTO_VIEWER, note)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else {
            setResult(Activity.RESULT_CANCELED, resultIntent)
            finish()
        }
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