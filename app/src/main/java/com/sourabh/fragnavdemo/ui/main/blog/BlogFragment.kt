package com.sourabh.fragnavdemo.ui.main.blog

import com.sourabh.fragnavdemo.R
import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import kotlinx.android.synthetic.main.fragment_blog.*

class BlogFragment : BaseBlogFragment()
{

//    private lateinit var searchView: SearchView
   // private lateinit var recyclerAdapter: BlogListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)
       // swipe_refresh.setOnRefreshListener(this)

        //initRecyclerView()
      //  subscribeObservers()

//        if(savedInstanceState == null){
//            viewModel.loadFirstPage()
//        }
    }

//    private fun subscribeObservers(){
//        viewModel.dataState.observe(viewLifecycleOwner, Observer{ dataState ->
//            if(dataState != null) {
//                // call before onDataStateChange to consume error if there is one
//                handlePagination(dataState)
//                stateChangeListener.onDataStateChange(dataState)
//            }
//        })
//
//        viewModel.viewState.observe(viewLifecycleOwner, Observer{ viewState ->
//            Log.d(TAG, "BlogFragment, ViewState: ${viewState}")
//            if(viewState != null){
//                recyclerAdapter.apply {
//                    preloadGlideImages(
//                        requestManager = requestManager,
//                        list = viewState.blogFields.blogList
//                    )
//                    submitList(
//                        blogList = viewState.blogFields.blogList,
//                        isQueryExhausted = viewState.blogFields.isQueryExhausted
//                    )
//                }
//
//            }
//        })
//    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
      //  initSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_filter_settings -> {
             //   showFilterDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}

















