package com.sourabh.fragnavdemo.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
import com.sourabh.fragnavdemo.api.main.OpenApiMainService
import com.sourabh.fragnavdemo.models.AccountProperties
import com.sourabh.fragnavdemo.models.AuthToken
import com.sourabh.fragnavdemo.persistence.AccountPropertiesDao
import com.sourabh.fragnavdemo.repository.NetworkBoundResource
import com.sourabh.fragnavdemo.session.SessionManager
import com.sourabh.fragnavdemo.ui.DataState
import com.sourabh.fragnavdemo.ui.main.account.state.AccountViewState
import com.sourabh.fragnavdemo.util.ApiSuccessResponse
import com.sourabh.fragnavdemo.util.GenericApiResponse
import kotlinx.coroutines.Job
import javax.inject.Inject
import androidx.lifecycle.switchMap
import com.sourabh.fragnavdemo.api.GenericResponse
import com.sourabh.fragnavdemo.repository.JobManager
import com.sourabh.fragnavdemo.ui.Response
import com.sourabh.fragnavdemo.ui.ResponseType
import com.sourabh.fragnavdemo.util.AbsentLiveData
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext


class AccountRepository
@Inject constructor(
        val openApiMainService: OpenApiMainService,
        val accountPropertiesDao: AccountPropertiesDao,
        val sessionManager: SessionManager
) : JobManager("AccountRepository")
{

    private  val TAG = "AppDebug"


    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>>{
        return object : NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                false,
                true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                updateLocalDB(response.body)

                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return openApiMainService.getAccountProperties("Token ${authToken.token})")
            }

            override fun setJob(job: Job) {
                addJob("getAccountProperties", job)
            }

            override suspend fun createCacheRequestAndReturn() {
                withContext(Main){

                    result.addSource(loadFromCache()) {
                        onCompleteJob(DataState.data(
                                data = it,
                                response = null
                        ))
                    }

                }
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return accountPropertiesDao.searchByPk(authToken.account_pk!!)
                        .switchMap {
                            object: LiveData<AccountViewState>(){
                                override fun onActive() {
                                    super.onActive()
                                    value = AccountViewState(it)
                                }
                            }
                        }
            }

            override fun updateLocalDB(cacheObject: AccountProperties?) {
                cacheObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                            cacheObject.pk,
                            cacheObject.email,
                            cacheObject.username
                    )
                }
            }

        }. asLiveData()
    }

    fun saveAccountProperties(authToken: AuthToken,
         accountProperties: AccountProperties) : LiveData<DataState<AccountViewState>>{

        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                true,
                false
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {

                withContext(Main) {

                    onCompleteJob(DataState.data(
                            data = null,
                            response = Response(response.body.response, ResponseType.Toast())
                    ))
                }

                updateLocalDB(null)
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.saveAccountProperties("Token ${authToken.token!!}"
                 , accountProperties.email, accountProperties.username)
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override fun updateLocalDB(cacheObject: Any?) {
                return accountPropertiesDao.updateAccountProperties(accountProperties.pk,
                accountProperties.email, accountProperties.username)
            }

            override fun setJob(job: Job) {
                addJob("saveAccountProperties", job)
            }

            override suspend fun createCacheRequestAndReturn() {
            }

        }. asLiveData()

    }

    fun updatePassword(authToken: AuthToken, currentPassword: String, newPassword: String,
                       confirmNewPassword: String): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                true,
                false
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                    withContext(Main){
                        onCompleteJob(
                                DataState.data(
                                        data = null,
                                        response = Response(response.body.response, ResponseType.Toast())
                                )
                        )
                    }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.updatePassword(
                        "Token ${authToken.token}",
                        currentPassword,
                        newPassword,
                        confirmNewPassword
                )
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                // not applicable
                return AbsentLiveData.create()
            }

            override fun updateLocalDB(cacheObject: Any?) {
                // not applicable
            }

            override fun setJob(job: Job) {
                addJob("updatePassword", job)

            }

            override suspend fun createCacheRequestAndReturn() {
                // not applicable in this case
            }

        }. asLiveData()
    }

}