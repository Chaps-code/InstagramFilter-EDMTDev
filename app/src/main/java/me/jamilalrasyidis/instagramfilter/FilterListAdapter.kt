package me.jamilalrasyidis.instagramfilter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import me.jamilalrasyidis.instagramfilter.interfaces.FilterListFragmentListener
import com.zomato.photofilters.utils.ThumbnailItem

class FilterListAdapter : RecyclerView.Adapter<FilterListAdapter.ViewHolder>() {

    private var filters = mutableListOf<ThumbnailItem>()
    var selectedIndex = -1
    var context: Context? = null
    var listener: FilterListFragmentListener? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(thumbnail: ThumbnailItem) {
            val textFilterName = itemView.findViewById<TextView>(R.id.text_filter_name)
            val imageFilterThumbnail = itemView.findViewById<ImageView>(R.id.image_filter_thumbnail)

            textFilterName.text = thumbnail.filterName

            if (selectedIndex == adapterPosition)
                textFilterName.setTextColor(
                    ContextCompat.getColor(
                        context!!,
                        R.color.selected_filter
                    )
                )
            else
                textFilterName.setTextColor(
                    ContextCompat.getColor(
                        context!!,
                        R.color.normal_filter
                    )
                )

            imageFilterThumbnail.setImageBitmap(thumbnail.image)
            imageFilterThumbnail.setOnClickListener {
                listener?.onFilterChanged(thumbnail.filter)
                selectedIndex = adapterPosition
                notifyDataSetChanged()
            }
        }
    }

    fun setFilterList(filters: MutableList<ThumbnailItem>) {
        this.filters = filters
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_thumbnail, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return filters.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filters[position])
    }
}