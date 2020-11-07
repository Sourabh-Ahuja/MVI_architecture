package com.sourabh.fragnavdemo.repository.auth

import androidx.lifecycle.LiveData
import com.sourabh.fragnavdemo.api.auth.OpenApiAuthService
import com.sourabh.fragnavdemo.api.auth.network_responses.LoginResponse
import com.sourabh.fragnavdemo.api.auth.network_responses.RegistrationResponse
import com.sourabh.fragnavdemo.persistence.AccountPropertiesDao
import com.sourabh.fragnavdemo.persistence.AuthTokenDao
import com.sourabh.fragnavdemo.session.SessionManager
import com.sourabh.fragnavdemo.ui.DataState
import com.sourabh.fragnavdemo.ui.auth.state.AuthViewState
import com.sourabh.fragnavdemo.util.GenericApiResponse
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager
)
{

    fun attemptLogin(email : String, password : String) : LiveData<DataState<AuthViewState>>{
        return openApiAuthService.login(email, password)
    }

    fun attemptRegistration(email : String, username : String, password: String
       , confirm_password : String) : LiveData<DataState<AuthViewState>>{
        return openApiAuthService.register(email,username,password,confirm_password)
    }

}
