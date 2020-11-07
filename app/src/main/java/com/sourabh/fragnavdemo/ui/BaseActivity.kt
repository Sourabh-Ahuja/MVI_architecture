package com.sourabh.fragnavdemo.ui

import com.sourabh.fragnavdemo.session.SessionManager
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity() {

    private val TAG = "BaseActivity"

    @Inject
    lateinit var sessionManager: SessionManager

}