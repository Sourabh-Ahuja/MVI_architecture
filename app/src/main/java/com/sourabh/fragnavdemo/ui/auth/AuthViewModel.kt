package com.sourabh.fragnavdemo.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.sourabh.fragnavdemo.api.auth.network_responses.LoginResponse
import com.sourabh.fragnavdemo.api.auth.network_responses.RegistrationResponse
import com.sourabh.fragnavdemo.models.AuthToken
import com.sourabh.fragnavdemo.repository.auth.AuthRepository
import com.sourabh.fragnavdemo.ui.BaseViewModel
import com.sourabh.fragnavdemo.ui.DataState
import com.sourabh.fragnavdemo.ui.Loading
import com.sourabh.fragnavdemo.ui.auth.state.AuthStateEvent
import com.sourabh.fragnavdemo.ui.auth.state.AuthStateEvent.*
import com.sourabh.fragnavdemo.ui.auth.state.AuthViewState
import com.sourabh.fragnavdemo.ui.auth.state.LoginFields
import com.sourabh.fragnavdemo.ui.auth.state.RegistrationFields
import com.sourabh.fragnavdemo.util.AbsentLiveData
import com.sourabh.fragnavdemo.util.GenericApiResponse
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    val authRepository: AuthRepository
) : BaseViewModel<AuthStateEvent,AuthViewState>()
{

    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }

    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {
        when(stateEvent) {
            is LoginAttemptEvent -> {
                return authRepository.attemptLogin(stateEvent.email, stateEvent.password)
            }
            is RegisterAttemptEvent -> {
                return authRepository.attemptRegistration(stateEvent.email,
                    stateEvent.username, stateEvent.password, stateEvent.confirm_password)
            }
            is CheckPreviousAuthEvent -> {
                return authRepository.checkPreviousAuthUser()
            }
            is None ->{
                return object : LiveData<DataState<AuthViewState>>(){
                    override fun onActive() {
                        super.onActive()
                        value = DataState.data(
                                data = null,
                                response = null
                        )
                    }
                }
            }
        }
    }

    fun setRegistrationFields(registrationFields: RegistrationFields){
        val update = getCurrentViewStateOrNew()
        if(update.registrationFields == registrationFields){
            return
        }
        update.registrationFields = registrationFields
        setViewState(update)
    }

    fun setLoginFields(loginFields: LoginFields){
        val update = getCurrentViewStateOrNew()
        if(update.loginFields == loginFields){
            return
        }
        update.loginFields = loginFields
        setViewState(update)
    }

    fun setAuthToken(authToken: AuthToken){
        val update = getCurrentViewStateOrNew()
        if(update.authToken == authToken){
            return
        }
        update.authToken = authToken
        setViewState(update)
    }

    fun cancelActiveJobs() {
        authRepository.cancelActiveJobs()
        handlePendingData()
    }

    fun handlePendingData(){
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}