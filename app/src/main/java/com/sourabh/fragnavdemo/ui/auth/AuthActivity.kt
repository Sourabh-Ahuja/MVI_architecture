package com.sourabh.fragnavdemo.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sourabh.fragnavdemo.R
import com.sourabh.fragnavdemo.ui.BaseActivity
import com.sourabh.fragnavdemo.ui.ResponseType
import com.sourabh.fragnavdemo.ui.ResponseType.*
import com.sourabh.fragnavdemo.ui.main.MainActivity
import com.sourabh.fragnavdemo.viewModels.ViewModelProviderFactory
import javax.inject.Inject

class AuthActivity : BaseActivity() {

    private val TAG = "AuthActivity"
    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)

        subscribeObserver()
    }

    fun subscribeObserver() {

        viewModel.dataState.observe(this, Observer {dateState ->
             dateState.data?.let {date ->
                 date.data?.let {event ->
                     event.getContentIfNotHandled()?.let {
                         it.authToken?.let {
                             Log.d(TAG,"Date State {$it}")
                             viewModel.setAuthToken(it)
                         }
                     }
                 }

                date.response?.let { event ->
                    event.getContentIfNotHandled()?.let {response ->
                        when(response.responseType){
                            is Toast -> {

                            }
                            is Dialog -> {

                            }

                            is None -> {

                            }
                        }
                    }
                }

             }
        })

        viewModel.viewState.observe(this, Observer{
            Log.d(TAG, "AuthActivity, subscribeObservers: AuthViewState: ${it}")
            it.authToken?.let{
                sessionManager.login(it)
            }
        })

        sessionManager.cachedToken.observe(this, Observer{ dataState ->
            Log.d(TAG, "AuthActivity, subscribeObservers: AuthDataState: ${dataState}")
            dataState.let{ authToken ->
                if(authToken != null && authToken.account_pk != -1 && authToken.token != null){
                    navMainActivity()
                }
            }
        })
    }

    private fun navMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent )
        finish()
    }
}