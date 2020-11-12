package com.sourabh.fragnavdemo.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.sourabh.fragnavdemo.api.GenericResponse
import com.sourabh.fragnavdemo.api.main.OpenApiMainService
import com.sourabh.fragnavdemo.api.main.respones.BlogListSearchResponse
import com.sourabh.fragnavdemo.models.AuthToken
import com.sourabh.fragnavdemo.models.BlogPost
import com.sourabh.fragnavdemo.persistence.BlogPostDao
import com.sourabh.fragnavdemo.persistence.returnOrderedBlogQuery
import com.sourabh.fragnavdemo.repository.JobManager
import com.sourabh.fragnavdemo.repository.NetworkBoundResource
import com.sourabh.fragnavdemo.session.SessionManager
import com.sourabh.fragnavdemo.ui.DataState
import com.sourabh.fragnavdemo.ui.main.blog.state.BlogViewState
import com.sourabh.fragnavdemo.ui.main.blog.state.BlogViewState.*
import com.sourabh.fragnavdemo.util.AbsentLiveData
import com.sourabh.fragnavdemo.util.ApiSuccessResponse
import com.sourabh.fragnavdemo.util.Constants.Companion.PAGINATION_PAGE_SIZE
import com.sourabh.fragnavdemo.util.DateUtils
import com.sourabh.fragnavdemo.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.sourabh.fragnavdemo.util.GenericApiResponse
import com.sourabh.fragnavdemo.util.SuccessHandling.Companion.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.sourabh.fragnavdemo.util.SuccessHandling.Companion.RESPONSE_NO_PERMISSION_TO_EDIT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class BlogRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): JobManager("BlogRepository")
{

    private val TAG: String = "AppDebug"

    fun searchBlogPosts(
            authToken: AuthToken,
            query: String,
            filterAndOrder: String,
            page: Int
    ): LiveData<DataState<BlogViewState>> {
        return object: NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                false,
                true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogListSearchResponse>) {
                val blogPostList: ArrayList<BlogPost> = ArrayList()
                for(blogPostResponse in response.body.results){
                    blogPostList.add(
                            BlogPost(
                                    pk = blogPostResponse.pk,
                                    title = blogPostResponse.title,
                                    slug = blogPostResponse.slug,
                                    body = blogPostResponse.body,
                                    image = blogPostResponse.image,
                                    date_updated = DateUtils.convertServerStringDateToLong(
                                            blogPostResponse.date_updated
                                    ),
                                    username = blogPostResponse.username
                            )
                    )
                }

                updateLocalDB(blogPostList)

                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogListSearchResponse>> {
                return openApiMainService.searchListBlogPosts("Token ${authToken.token}"
                       ,query,
                        ordering = filterAndOrder,
                        page = page
                   )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
               return blogPostDao.returnOrderedBlogQuery(
                       query = query,
                       filterAndOrder = filterAndOrder,
                       page = page
               )
                       .switchMap {
                           object : LiveData<BlogViewState>() {
                               override fun onActive() {
                                   super.onActive()
                                    value = BlogViewState(
                                            BlogFields(
                                                    blogList = it,
                                                    isQueryInProgress = true
                                            )
                                    )
                               }
                           }
                       }
            }

            override suspend fun updateLocalDB(cacheObject: List<BlogPost>?) {
                if(cacheObject != null){
                    withContext(IO) {
                        for(blogPost in cacheObject){
                            try{
                                // Launch each insert as a separate job to be executed in parallel
                                launch {
                                    Log.d(TAG, "updateLocalDb: inserting blog: ${blogPost}")
                                    blogPostDao.insert(blogPost)
                                }
                            }catch (e: Exception){
                                Log.e(TAG, "updateLocalDb: error updating cache data on blog post with slug: ${blogPost.slug}. " +
                                        "${e.message}")
                                // Could send an error report here or something but I don't think you should throw an error to the UI
                                // Since there could be many blog posts being inserted/updated.
                            }
                        }
                    }
                }
                else{
                    Log.d(TAG, "updateLocalDb: blog post list is null")
                }
            }

            override fun setJob(job: Job) {
                addJob("searchBlogPosts ",job)
            }

            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main){

                    // finishing by viewing db cache
                    result.addSource(loadFromCache()){ viewState ->
                        viewState.blogFields.isQueryInProgress = false
                        if(page * PAGINATION_PAGE_SIZE > viewState.blogFields.blogList.size){
                            viewState.blogFields.isQueryExhausted = true
                        }
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }
            // if network is down, view cache only and return

        }.asLiveData()
    }

    fun isAuthorOfBlogPost(
        authToken: AuthToken,
        slug: String
    ): LiveData<DataState<BlogViewState>> {
        return object: NetworkBoundResource<GenericResponse, Any, BlogViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ){


            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Dispatchers.Main){

                    Log.d(TAG, "handleApiSuccessResponse: ${response.body.response}")
                    if(response.body.response.equals(RESPONSE_NO_PERMISSION_TO_EDIT)){
                        onCompleteJob(
                            DataState.data(
                                data = BlogViewState(
                                    viewBlogFields = ViewBlogFields(
                                        isAuthorOfBlogPost = false
                                    )
                                ),
                                response = null
                            )
                        )
                    }
                    else if(response.body.response.equals(RESPONSE_HAS_PERMISSION_TO_EDIT)){
                        onCompleteJob(
                            DataState.data(
                                data = BlogViewState(
                                    viewBlogFields = ViewBlogFields(
                                        isAuthorOfBlogPost = true
                                    )
                                ),
                                response = null
                            )
                        )
                    }
                    else{
                        onErrorReturn(ERROR_UNKNOWN, shouldUseDialog = false, shouldUseToast = false)
                    }
                }
            }

            // not applicable
            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            // Make an update and change nothing.
            // If they are not the author it will return: "You don't have permission to edit that."
            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.isAuthorOfBlogPost(
                    "Token ${authToken.token!!}",
                    slug
                )
            }

            override suspend fun updateLocalDB(cacheObject: Any?) {

            }

            override fun setJob(job: Job) {
                addJob("isAuthorOfBlogPost", job)
            }


        }.asLiveData()
    }

}
















