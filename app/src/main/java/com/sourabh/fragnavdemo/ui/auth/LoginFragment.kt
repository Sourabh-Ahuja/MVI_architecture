package com.sourabh.fragnavdemo.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.sourabh.fragnavdemo.R
import com.sourabh.fragnavdemo.models.AuthToken
import com.sourabh.fragnavdemo.ui.auth.state.AuthStateEvent
import com.sourabh.fragnavdemo.ui.auth.state.AuthStateEvent.*
import com.sourabh.fragnavdemo.ui.auth.state.LoginFields
import kotlinx.android.synthetic.main.fragment_login.*

class  LoginFragment : BaseAuthFragment() {

    private val TAG = "LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObserver()

        login_button.setOnClickListener{
            login()
        }
    }

    fun subscribeObserver() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            it.loginFields?.let {
               it.login_email?.let {
                   input_email.setText(it)
               }
                it.login_password?.let {
                    input_password.setText(it)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setLoginFields(
            LoginFields(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }

    fun login(){
        viewModel.setStateEvent(
            LoginAttemptEvent(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }
}
