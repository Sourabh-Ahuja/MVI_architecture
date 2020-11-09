package com.sourabh.fragnavdemo.ui.main.blog

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.sourabh.fragnavdemo.models.BlogPost
import com.sourabh.fragnavdemo.repository.main.BlogRepository
import com.sourabh.fragnavdemo.session.SessionManager
import com.sourabh.fragnavdemo.ui.BaseViewModel
import com.sourabh.fragnavdemo.ui.DataState
import com.sourabh.fragnavdemo.ui.main.blog.state.BlogStateEvent
import com.sourabh.fragnavdemo.ui.main.blog.state.BlogViewState
import com.sourabh.fragnavdemo.util.AbsentLiveData
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
        private val sessionManager: SessionManager,
        private val blogRepository: BlogRepository,
        private val sharedPreferences: SharedPreferences,
        private val requestManager : RequestManager
): BaseViewModel<BlogStateEvent, BlogViewState>() {

    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
          when(stateEvent) {
              is BlogStateEvent.BlogSearchEvent -> {
                  return AbsentLiveData.create()
              }
              is BlogStateEvent.None -> {
                  return AbsentLiveData.create()
              }
          }
    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    fun BlogViewModel.setQuery(query: String){
        val update = getCurrentViewStateOrNew()
        update.blogFields.searchQuery = query
        setViewState(update)
    }

    fun BlogViewModel.setBlogListData(blogList: List<BlogPost>){
        val update = getCurrentViewStateOrNew()
        update.blogFields.blogList = blogList
        setViewState(update)
    }

    fun cancelActiveJobs(){
        blogRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    fun handlePendingData(){
        setStateEvent(BlogStateEvent.None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}