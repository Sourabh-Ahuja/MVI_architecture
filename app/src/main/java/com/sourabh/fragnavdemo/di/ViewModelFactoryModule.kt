package com.sourabh.fragnavdemo.di


import androidx.lifecycle.ViewModelProvider
import com.sourabh.fragnavdemo.viewModels.ViewModelProviderFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory
}