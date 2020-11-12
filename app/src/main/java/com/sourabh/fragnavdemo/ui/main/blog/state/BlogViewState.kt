package com.sourabh.fragnavdemo.ui.main.blog.state

import android.net.Uri
import com.sourabh.fragnavdemo.models.BlogPost
import com.sourabh.fragnavdemo.persistence.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.sourabh.fragnavdemo.persistence.BlogQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED


data class BlogViewState (

        // BlogFragment vars
        var blogFields: BlogFields = BlogFields(),

        // ViewBlogFragment vars
        var viewBlogFields: ViewBlogFields = ViewBlogFields()
//
//        // UpdateBlogFragment vars
//        var updatedBlogFields: UpdatedBlogFields = UpdatedBlogFields()
)
{
    data class BlogFields(
            var blogList: List<BlogPost> = ArrayList(),
            var searchQuery: String = "",
            var page: Int = 1,
            var isQueryInProgress: Boolean = false,
            var isQueryExhausted: Boolean = false,
            var filter: String = ORDER_BY_ASC_DATE_UPDATED,
            var order: String = BLOG_ORDER_ASC
    )

    data class ViewBlogFields(
            var blogPost: BlogPost? = null,
            var isAuthorOfBlogPost: Boolean = false
    )

//    data class UpdatedBlogFields(
//            var updatedBlogTitle: String? = null,
//            var updatedBlogBody: String? = null,
//            var updatedImageUri: Uri? = null
//    )
}








