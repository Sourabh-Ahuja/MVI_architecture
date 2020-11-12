package com.sourabh.fragnavdemo.ui.main.blog.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.codingwithmitch.openapi.ui.main.blog.viewmodel.*
import com.sourabh.fragnavdemo.persistence.BlogQueryUtils
import com.sourabh.fragnavdemo.repository.main.BlogRepository
import com.sourabh.fragnavdemo.session.SessionManager
import com.sourabh.fragnavdemo.ui.BaseViewModel
import com.sourabh.fragnavdemo.ui.DataState
import com.sourabh.fragnavdemo.ui.Loading
import com.sourabh.fragnavdemo.ui.main.blog.state.BlogStateEvent
import com.sourabh.fragnavdemo.ui.main.blog.state.BlogStateEvent.*
import com.sourabh.fragnavdemo.ui.main.blog.state.BlogViewState
import com.sourabh.fragnavdemo.util.AbsentLiveData
import com.sourabh.fragnavdemo.util.PreferenceKeys.Companion.BLOG_FILTER
import com.sourabh.fragnavdemo.util.PreferenceKeys.Companion.BLOG_ORDER
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
        private val sessionManager: SessionManager,
        private val blogRepository: BlogRepository,
        private val sharedPreferences: SharedPreferences,
        private val editor : SharedPreferences.Editor
): BaseViewModel<BlogStateEvent, BlogViewState>() {

    init {
        setBlogFilter(
                sharedPreferences.getString(
                        BLOG_FILTER,
                        BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
                )
        )

        setBlogOrder(
                sharedPreferences.getString(
                        BLOG_ORDER,
                        BlogQueryUtils.BLOG_ORDER_ASC
                )
        )


    }

    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
          when(stateEvent) {
              is BlogSearchEvent -> {
                  return sessionManager.cachedToken.value?.let {
                      blogRepository.searchBlogPosts(authToken = it  ,
                      query = getSearchQuery(),
                      filterAndOrder = getOrder() + getFilter(),
                      page = getPage())
                  }?: AbsentLiveData.create()
              }
              is CheckAuthorOfBlogPost -> {
                  return sessionManager.cachedToken.value?.let { authToken ->
                      blogRepository.isAuthorOfBlogPost(
                          authToken = authToken,
                          slug = getSlug()
                      )
                  }?: AbsentLiveData.create()
              }
              is None -> {
                  return object : LiveData<DataState<BlogViewState>>() {
                      override fun onActive() {
                          super.onActive()
                          value = DataState(
                                  null,
                                  Loading(false),
                                  null
                          )
                      }
                  }

              }
          }
    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    fun cancelActiveJobs(){
        blogRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    fun handlePendingData(){
        setStateEvent(None())
    }

    fun saveFilterOptions(filter: String, order: String){
        editor.putString(BLOG_FILTER, filter)
        editor.apply()

        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}