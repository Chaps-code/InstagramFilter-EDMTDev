package me.jamilalrasyidis.instagramfilter.interfaces

import com.zomato.photofilters.imageprocessors.Filter

interface FilterListFragmentListener {
    fun onFilterChanged(filter: Filter)
}