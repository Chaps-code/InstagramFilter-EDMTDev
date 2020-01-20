package me.jamilalrasyidis.instagramfilter

import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.jamilalrasyidis.instagramfilter.interfaces.FilterListFragmentListener
import me.jamilalrasyidis.instagramfilter.utils.addSpaceItem
import me.jamilalrasyidis.instagramfilter.utils.loadBitmapFromAsset
import com.zomato.photofilters.FilterPack
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.utils.ThumbnailItem
import com.zomato.photofilters.utils.ThumbnailsManager

class FilterListFragment : Fragment(),
    FilterListFragmentListener {

    private var listener: FilterListFragmentListener? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var thumbnailItems: MutableList<ThumbnailItem>

    private val adapter by lazy { FilterListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_filter_list, container, false)
        recyclerView = view.findViewById(R.id.list_filter)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thumbnailItems = mutableListOf()
        adapter.setFilterList(thumbnailItems)
        adapter.listener = this
        adapter.context = activity

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.itemAnimator = DefaultItemAnimator()
        val space =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, resources.displayMetrics)
                .toInt()
        recyclerView.addSpaceItem(space)
        recyclerView.adapter = adapter

        displayThumbnail(null)
    }

    fun displayThumbnail(bitmap: Bitmap?) {
        val r = Runnable {
            val thumbImg: Bitmap = (if (bitmap == null)
                requireContext().loadBitmapFromAsset(MainActivity.IMAGE_FILENAME, 100, 100)
            else
                Bitmap.createScaledBitmap(bitmap, 100, 100, false))
                ?: return@Runnable
            ThumbnailsManager.clearThumbs()
            thumbnailItems.clear()

            val thumbnailItem = ThumbnailItem().apply {
                filterName = "Normal"
                image = thumbImg
            }
            ThumbnailsManager.addThumb(thumbnailItem)

            val filters: List<Filter> = FilterPack.getFilterPack(requireContext())

            for (filter in filters) {
                val thumbItem = ThumbnailItem()
                thumbItem.filterName = filter.name
                thumbItem.image = thumbImg
                thumbItem.filter = filter
                ThumbnailsManager.addThumb(thumbItem)
            }

            thumbnailItems.addAll(ThumbnailsManager.processThumbs(requireContext()))

            activity?.runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        }
        Thread(r).start()
    }

    fun setListener(listener: FilterListFragmentListener) {
        this.listener = listener
    }

    override fun onFilterChanged(filter: Filter) {
        if (listener != null) {
            listener?.onFilterChanged(filter)
        }
    }
}