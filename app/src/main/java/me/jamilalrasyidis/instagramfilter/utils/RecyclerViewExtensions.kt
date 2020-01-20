package me.jamilalrasyidis.instagramfilter.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.addSpaceItem(space: Int) {
    this.addItemDecoration(object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.getChildAdapterPosition(view) == state.itemCount - 1) {
                outRect.left = space
                outRect.right = 0
            } else {
                outRect.left = 0
                outRect.right = space
            }
        }
    })
}