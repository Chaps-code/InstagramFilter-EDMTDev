package com.abcode.instagramfilter.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import java.io.FileNotFoundException
import java.io.IOException

fun Bitmap?.insertImage(
    cr: ContentResolver,
    title: String,
    desc: String?
): String? {
    val contentValues = ContentValues()
    contentValues.put(MediaStore.Images.Media.TITLE, title)
    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, title)
    contentValues.put(MediaStore.Images.Media.DESCRIPTION, desc)
    contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
    contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

    var uri: Uri? = null
    var stringUrl: String? = null

    try {
        uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (this != null) {
            val outputStream = cr.openOutputStream(uri!!)
            try {
                this.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            } finally {
                outputStream?.close()
                val id = ContentUris.parseId(uri)
                @Suppress("DEPRECATION") val miniThumb: Bitmap =
                    MediaStore.Images.Thumbnails.getThumbnail(
                        cr,
                        id,
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        null
                    )

                @Suppress("DEPRECATION")
                storeThumbnail(cr, miniThumb, id, 50f, 50f, MediaStore.Images.Thumbnails.MICRO_KIND)
            }
        } else {
            cr.delete(uri!!, null, null)
            uri = null
        }
    } catch (e: FileNotFoundException) {
        if (uri != null) {
            cr.delete(uri, null, null)
            uri = null
        }
    }
    if (uri != null) stringUrl = uri.toString()

    return stringUrl
}

@Suppress("DEPRECATION")
private fun storeThumbnail(
    cr: ContentResolver,
    source: Bitmap,
    id: Long,
    width: Float,
    height: Float,
    microKind: Int
): Bitmap? {
    val matrix = Matrix()
    val scaleX = width / source.width
    val scaleY = height / source.height

    matrix.setScale(scaleX, scaleY)
    val thumb = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    val contentValues = ContentValues(4)
    contentValues.put(MediaStore.Images.Thumbnails.KIND, microKind)
    contentValues.put(MediaStore.Images.Thumbnails.IMAGE_ID, id)
    contentValues.put(MediaStore.Images.Thumbnails.HEIGHT, height)
    contentValues.put(MediaStore.Images.Thumbnails.WIDTH, width)

    val uri = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, contentValues)

    return try {
        val outputStream = cr.openOutputStream(uri!!)
        thumb.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream?.close()

        thumb
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun Bitmap.drawableToBitmap(drawable: Drawable): Bitmap? {

    if (drawable is BitmapDrawable) {
        val bitmapDrawable: BitmapDrawable = drawable
        if (bitmapDrawable.bitmap != null) {
            return bitmapDrawable.bitmap
        }
    }

    val bitmap: Bitmap? = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    } else {
        Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
    }

    val canvas = bitmap?.let { Canvas(it) }
    if (canvas != null) {
        drawable.setBounds(0, 0, canvas.width, canvas.height)
    }
    if (canvas != null) {
        drawable.draw(canvas)
    }
    return bitmap
}