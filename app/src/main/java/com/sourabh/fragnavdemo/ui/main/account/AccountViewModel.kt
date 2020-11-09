package com.sourabh.fragnavdemo.ui.main.account

import androidx.lifecycle.LiveData
import com.sourabh.fragnavdemo.models.AccountProperties
import com.sourabh.fragnavdemo.repository.main.AccountRepository
import com.sourabh.fragnavdemo.session.SessionManager
import com.sourabh.fragnavdemo.ui.BaseViewModel
import com.sourabh.fragnavdemo.ui.DataState
import com.sourabh.fragnavdemo.ui.auth.state.AuthStateEvent
import com.sourabh.fragnavdemo.ui.main.account.state.AccountStateEvent
import com.sourabh.fragnavdemo.ui.main.account.state.AccountStateEvent.*
import com.sourabh.fragnavdemo.ui.main.account.state.AccountViewState
import com.sourabh.fragnavdemo.util.AbsentLiveData
import javax.inject.Inject

class AccountViewModel
@Inject constructor(
        val sessionManager: SessionManager,
        val accountRepository: AccountRepository
) : BaseViewModel<AccountStateEvent, AccountViewState>() {

    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>> {
            when(stateEvent){
                is GetAccountPropertiesEvent -> {
                    return sessionManager.cachedToken.value?.let {
                       accountRepository.getAccountProperties(authToken = it)
                    }?: AbsentLiveData.create()
                }
                is UpdateAccountPropertiesEvent -> {
                    return sessionManager.cachedToken.value?.let { authToken ->
                        authToken.account_pk?.let { pk ->
                            val newAccountProperties = AccountProperties(
                                    pk,
                                    stateEvent.email,
                                    stateEvent.username
                            )
                            accountRepository.saveAccountProperties(
                                    authToken,
                                    newAccountProperties
                            )
                        }
                    }?: AbsentLiveData.create()
                }
                is ChangePasswordEvent -> {
                    return sessionManager.cachedToken.value?.let { authToken ->
                            accountRepository.updatePassword(
                                    authToken,
                                    stateEvent.currentPassword,
                                    stateEvent.newPassword,
                                    stateEvent.confirmNewPassword
                            )
                    }?: AbsentLiveData.create()
                }
                is None -> {
                    return AbsentLiveData.create()
                }
            }
    }

    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties){
        val update = getCurrentViewStateOrNew()
        if(update.accountProperties == accountProperties){
            return
        }
        update.accountProperties = accountProperties;
        _viewState.value = update
    }

    fun logout() {
        sessionManager.logout()
    }

    fun cancelActiveJobs() {
        accountRepository.cancelActiveJobs()
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