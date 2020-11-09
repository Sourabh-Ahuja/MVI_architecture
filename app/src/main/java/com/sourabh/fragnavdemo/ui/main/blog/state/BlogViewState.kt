package com.sourabh.fragnavdemo.ui.main.blog.state

import android.net.Uri
import com.sourabh.fragnavdemo.models.BlogPost


data class BlogViewState (

        // BlogFragment vars
        var blogFields: BlogFields = BlogFields(),

        // ViewBlogFragment vars
//        var viewBlogFields: ViewBlogFields = ViewBlogFields(),
//
//        // UpdateBlogFragment vars
//        var updatedBlogFields: UpdatedBlogFields = UpdatedBlogFields()
)
{
    data class BlogFields(
            var blogList: List<BlogPost> = ArrayList(),
            var searchQuery: String = ""
//            var page: Int = 1,
//            var isQueryInProgress: Boolean = false,
//            var isQueryExhausted: Boolean = false,
    )

//    data class ViewBlogFields(
//            var blogPost: BlogPost? = null,
//            var isAuthorOfBlogPost: Boolean = false
//    )
//
//    data class UpdatedBlogFields(
//            var updatedBlogTitle: String? = null,
//            var updatedBlogBody: String? = null,
//            var updatedImageUri: Uri? = null
//    )
}








