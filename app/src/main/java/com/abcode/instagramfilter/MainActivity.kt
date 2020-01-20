package com.abcode.instagramfilter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager
import com.abcode.instagramfilter.interfaces.EditImageFragmentListener
import com.abcode.instagramfilter.interfaces.FilterListFragmentListener
import com.abcode.instagramfilter.utils.insertImage
import com.abcode.instagramfilter.utils.loadBitmapFromAsset
import com.abcode.instagramfilter.utils.loadBitmapFromGallery
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter
import java.io.IOException

class MainActivity : AppCompatActivity(), FilterListFragmentListener, EditImageFragmentListener {

    companion object {
        const val IMAGE_FILENAME = "viloid.jpg"
        const val PERMISSION_PICK_IMAGE = 1000

        init {
            System.loadLibrary("NativeImageProcessor")
        }
    }

    private lateinit var imagePreview: ImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var constraintLayout: ConstraintLayout

    private lateinit var originalBitmap: Bitmap
    private lateinit var filteredBitmap: Bitmap
    private lateinit var finalBitmap: Bitmap

    private var brightnessFinal = 0
    private var contrastFinal = 1f
    private var saturationFinal = 1f

    private var filterListFragment: FilterListFragment? = null
    private var editImageFragment: EditImageFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Instagram Filter"

        imagePreview = findViewById(R.id.image_preview)
        tabLayout = findViewById(R.id.tabs)
        viewPager = findViewById(R.id.view_pager)
        constraintLayout = findViewById(R.id.constraint_layout)

        loadImage()

        setupViewPager(viewPager)
    }

    private fun loadImage() {
        originalBitmap = loadBitmapFromAsset(IMAGE_FILENAME, 300, 300)!!
        filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        finalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        imagePreview.setImageBitmap(originalBitmap)
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        val adapter = ViewPagerAdapter(supportFragmentManager)

        filterListFragment = FilterListFragment()
        filterListFragment?.setListener(this)

        editImageFragment = EditImageFragment()
        editImageFragment?.setListener(this)

        adapter.fragments.add(filterListFragment!!)
        adapter.titles.add("Filters")

        adapter.fragments.add(editImageFragment!!)
        adapter.titles.add("Edit Images")

        viewPager?.adapter = adapter

        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onFilterChanged(filter: Filter) {
        resetControl()
        filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        imagePreview.setImageBitmap(filter.processFilter(filteredBitmap))
        finalBitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun resetControl() {
        if (editImageFragment != null) editImageFragment?.resetControls()
        brightnessFinal = 0
        contrastFinal = 1f
        saturationFinal = 1f
    }

    override fun onBrightnessChanged(brightness: Int) {
        brightnessFinal = brightness
        val myFilter = Filter()
        myFilter.addSubFilter(BrightnessSubFilter(brightness))
        imagePreview.setImageBitmap(
            myFilter.processFilter(
                finalBitmap.copy(
                    Bitmap.Config.ARGB_8888,
                    true
                )
            )
        )
    }

    override fun onSaturationChanged(saturation: Float) {
        saturationFinal = saturation
        val myFilter = Filter()
        myFilter.addSubFilter(SaturationSubfilter(saturation))
        imagePreview.setImageBitmap(
            myFilter.processFilter(
                finalBitmap.copy(
                    Bitmap.Config.ARGB_8888,
                    true
                )
            )
        )
    }

    override fun onContrastChanged(contrast: Float) {
        contrastFinal = contrast
        val myFilter = Filter()
        myFilter.addSubFilter(ContrastSubFilter(contrast))
        imagePreview.setImageBitmap(
            myFilter.processFilter(
                finalBitmap.copy(
                    Bitmap.Config.ARGB_8888,
                    true
                )
            )
        )
    }

    override fun onEditStarted() {}

    override fun onEditCompleted() {
        val bitmap = filteredBitmap.copy(Bitmap.Config.ARGB_8888, true)

        val myFilter = Filter().apply {
            addSubFilter(BrightnessSubFilter(brightnessFinal))
            addSubFilter(ContrastSubFilter(contrastFinal))
            addSubFilter(SaturationSubfilter(saturationFinal))
        }

        finalBitmap = myFilter.processFilter(bitmap)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_open -> {
                openImageFromGallery()
                return true
            }
            R.id.action_save -> {
                saveImageToGallery()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun openImageFromGallery() {
        Dexter.withActivity(this)
            .withPermissions(
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted()!!) {
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        startActivityForResult(intent, PERMISSION_PICK_IMAGE)
                    } else {
                        Toast.makeText(this@MainActivity, "Permission Denied!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                }

            })
            .check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PERMISSION_PICK_IMAGE) {
            val bitmap = loadBitmapFromGallery(data?.data!!, 800, 800)

            originalBitmap.recycle()
            filteredBitmap.recycle()
            finalBitmap.recycle()

            originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            filteredBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            finalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

            imagePreview.setImageBitmap(originalBitmap)
            bitmap.recycle()

            filterListFragment?.displayThumbnail(originalBitmap)

        }
    }

    private fun saveImageToGallery() {
        Dexter.withActivity(this)
            .withPermissions(
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report?.areAllPermissionsGranted()!!) {
                        try {
                            val path: String? = finalBitmap.insertImage(
                                contentResolver,
                                System.currentTimeMillis().toString() + "_profile.jpg",
                                null
                            )
                            if (!TextUtils.isEmpty(path)) {
                                val snackBar = Snackbar.make(
                                    constraintLayout,
                                    "Image saved to gallery!",
                                    Snackbar.LENGTH_LONG
                                ).apply {
                                    setAction("Open") {
                                        openImage(path)
                                    }
                                }
                                snackBar.show()
                            } else {
                                val snackBar = Snackbar.make(
                                    constraintLayout,
                                    "Unable to save image",
                                    Snackbar.LENGTH_LONG
                                )
                                snackBar.show()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

            })
            .check()
    }

    private fun openImage(path: String?) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(Uri.parse(path), "image/*")
        startActivity(intent)
    }
}
