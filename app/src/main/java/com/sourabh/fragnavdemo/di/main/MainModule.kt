package com.sourabh.fragnavdemo.di.main

import com.sourabh.fragnavdemo.api.main.OpenApiMainService
import com.sourabh.fragnavdemo.persistence.AccountPropertiesDao
import com.sourabh.fragnavdemo.persistence.AppDatabase
import com.sourabh.fragnavdemo.persistence.BlogPostDao
import com.sourabh.fragnavdemo.repository.main.AccountRepository
import com.sourabh.fragnavdemo.repository.main.BlogRepository
import com.sourabh.fragnavdemo.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideOpenApiMainService(retrofitBuilder: Retrofit.Builder): OpenApiMainService {
        return retrofitBuilder
            .build()
            .create(OpenApiMainService::class.java)
    }

    @MainScope
    @Provides
    fun provideAccountRepository(
            openApiMainService: OpenApiMainService,
            accountPropertiesDao: AccountPropertiesDao,
            sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepository(openApiMainService, accountPropertiesDao, sessionManager)
    }
//
    @MainScope
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    @MainScope
    @Provides
    fun provideBlogRepository(
        openApiMainService: OpenApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepository(openApiMainService, blogPostDao, sessionManager)
    }
//
//    @MainScope
//    @Provides
//    fun provideCreateBlogRepository(
//        openApiMainService: OpenApiMainService,
//        blogPostDao: BlogPostDao,
//        sessionManager: SessionManager
//    ): CreateBlogRepository {
//        return CreateBlogRepository(openApiMainService, blogPostDao, sessionManager)
//    }
}

















