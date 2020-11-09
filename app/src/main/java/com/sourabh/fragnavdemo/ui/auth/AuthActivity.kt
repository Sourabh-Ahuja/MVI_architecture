package com.sourabh.fragnavdemo.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.sourabh.fragnavdemo.R
import com.sourabh.fragnavdemo.ui.BaseActivity
import com.sourabh.fragnavdemo.ui.ResponseType
import com.sourabh.fragnavdemo.ui.ResponseType.*
import com.sourabh.fragnavdemo.ui.auth.state.AuthStateEvent
import com.sourabh.fragnavdemo.ui.main.MainActivity
import com.sourabh.fragnavdemo.viewModels.ViewModelProviderFactory
import kotlinx.android.synthetic.main.activity_main.progress_bar
import javax.inject.Inject

class AuthActivity : BaseActivity(),
   NavController.OnDestinationChangedListener
{

    private val TAG = "AuthActivity"


    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)

        findNavController(R.id.auth_nav_host_fragment).addOnDestinationChangedListener(this)
        subscribeObserver()
    }

    override fun expandAppBar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }

    override fun onResume() {
        super.onResume()
        checkPrevisouAuthUser()
    }

    fun subscribeObserver() {

        viewModel.dataState.observe(this, Observer {dateState ->
            onDataStateChange(dateState)
             dateState.data?.let {date ->
                 date.data?.let {event ->
                     event.getContentIfNotHandled()?.let {
                         it.authToken?.let {
                             Log.d(TAG,"Date State {$it}")
                             viewModel.setAuthToken(it)
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

    fun checkPrevisouAuthUser() {
        viewModel.setStateEvent(
            AuthStateEvent.CheckPreviousAuthEvent()
        )
    }

    private fun navMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent )
        finish()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        viewModel.cancelActiveJobs()
    }

    override fun displayProgressBar(boolean: Boolean) {
        if(boolean){
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.INVISIBLE
        }
    }
}