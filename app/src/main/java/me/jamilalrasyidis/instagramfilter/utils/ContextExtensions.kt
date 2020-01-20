package me.jamilalrasyidis.instagramfilter.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.provider.MediaStore
import java.lang.Exception

fun Context.loadBitmapFromGallery(
    uri: Uri,
    width: Int,
    height: Int
): Bitmap {
    @Suppress("DEPRECATION") val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = this.contentResolver.query(uri, filePathColumn, null, null, null)
    cursor?.moveToFirst()
    val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
    val picturePath = cursor?.getString(columnIndex!!)
    cursor?.close()

    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(picturePath, options)
    options.inSampleSize =
        calculateInSampleSize(
            options,
            width,
            height
        )
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeFile(picturePath, options)
}

fun Context.loadBitmapFromAsset(
    filename: String,
    width: Int,
    height: Int
): Bitmap? {
    return try {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val inputStream = this.assets.open(filename)
        options.inSampleSize =
            calculateInSampleSize(
                options,
                width,
                height
            )
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeStream(inputStream, null, options)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun Bitmap.applyOverlay(
    context: Context,
    sourceImage: Bitmap,
    overlayDrawableSourceId: Int
): Bitmap? {
    var bitmap: Bitmap?

    try {
        val width = sourceImage.width
        val height = sourceImage.height
        val r = context.resources

        val imageAsDrawable = BitmapDrawable(r, sourceImage)
        val layers = mutableListOf<Drawable>()

        layers[0] = imageAsDrawable
        layers[1] = BitmapDrawable(
            r,
            decodeSampleBitmapFromResources(r, overlayDrawableSourceId, width, height)
        )
        val layerDrawable = LayerDrawable(layers.toTypedArray())

        bitmap = drawableToBitmap(layerDrawable)
    } catch (e: Exception) {
        bitmap = null
    }

    return bitmap
}

fun Bitmap.decodeSampleBitmapFromResources(
    res: Resources,
    resId: Int,
    reqWidth: Int,
    reqHeight: Int
): Bitmap {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeResource(res, resId, options)
    options.inSampleSize =
        calculateInSampleSize(
            options,
            reqWidth,
            reqHeight
        )
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeResource(res, resId, options)
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = -1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}