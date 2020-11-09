package com.sourabh.fragnavdemo.repository.main

import com.sourabh.fragnavdemo.api.main.OpenApiMainService
import com.sourabh.fragnavdemo.persistence.BlogPostDao
import com.sourabh.fragnavdemo.repository.JobManager
import com.sourabh.fragnavdemo.session.SessionManager
import javax.inject.Inject

class BlogRepository
@Inject
constructor(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
) : JobManager("BlogRepository") {

    private val TAG : String = "AppDebug"
}