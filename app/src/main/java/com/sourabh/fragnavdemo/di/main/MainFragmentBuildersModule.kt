package com.sourabh.fragnavdemo.di.main


import com.sourabh.fragnavdemo.ui.main.account.AccountFragment
import com.sourabh.fragnavdemo.ui.main.account.ChangePasswordFragment
import com.sourabh.fragnavdemo.ui.main.account.UpdateAccountFragment
import com.sourabh.fragnavdemo.ui.main.blog.BlogFragment
import com.sourabh.fragnavdemo.ui.main.blog.UpdateBlogFragment
import com.sourabh.fragnavdemo.ui.main.blog.ViewBlogFragment
import com.sourabh.fragnavdemo.ui.main.create_blog.CreateBlogFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeBlogFragment(): BlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeAccountFragment(): AccountFragment

    @ContributesAndroidInjector()
    abstract fun contributeChangePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector()
    abstract fun contributeCreateBlogFragment(): CreateBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateBlogFragment(): UpdateBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeViewBlogFragment(): ViewBlogFragment

    @ContributesAndroidInjector()
    abstract fun contributeUpdateAccountFragment(): UpdateAccountFragment
}