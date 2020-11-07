package com.sourabh.fragnavdemo.di.auth

import com.sourabh.fragnavdemo.ui.auth.ForgotPasswordFragment
import com.sourabh.fragnavdemo.ui.auth.LauncherFragment
import com.sourabh.fragnavdemo.ui.auth.LoginFragment
import com.sourabh.fragnavdemo.ui.auth.RegisterFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthFragmentBuildersModule {

    @ContributesAndroidInjector()
    abstract fun contributeLauncherFragment(): LauncherFragment

    @ContributesAndroidInjector()
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector()
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector()
    abstract fun contributeForgotPasswordFragment(): ForgotPasswordFragment

}