package com.sourabh.fragnavdemo.di

import com.sourabh.fragnavdemo.di.auth.AuthFragmentBuildersModule
import com.sourabh.fragnavdemo.di.auth.AuthModule
import com.sourabh.fragnavdemo.di.auth.AuthScope
import com.sourabh.fragnavdemo.di.auth.AuthViewModelModule
import com.sourabh.fragnavdemo.di.main.MainFragmentBuildersModule
import com.sourabh.fragnavdemo.di.main.MainModule
import com.sourabh.fragnavdemo.di.main.MainScope
import com.sourabh.fragnavdemo.di.main.MainViewModelModule
import com.sourabh.fragnavdemo.ui.auth.AuthActivity
import com.sourabh.fragnavdemo.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @AuthScope
    @ContributesAndroidInjector(
        modules = [AuthModule::class, AuthFragmentBuildersModule::class, AuthViewModelModule::class]
    )
    abstract fun contributeAuthActivity(): AuthActivity

    @MainScope
    @ContributesAndroidInjector(
        modules = [MainModule::class, MainFragmentBuildersModule::class, MainViewModelModule::class]
    )
    abstract fun contributeMainActivity(): MainActivity
}
