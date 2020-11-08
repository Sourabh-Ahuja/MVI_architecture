package com.sourabh.fragnavdemo.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.sourabh.fragnavdemo.api.auth.OpenApiAuthService
import com.sourabh.fragnavdemo.api.auth.network_responses.LoginResponse
import com.sourabh.fragnavdemo.api.auth.network_responses.RegistrationResponse
import com.sourabh.fragnavdemo.models.AccountProperties
import com.sourabh.fragnavdemo.models.AuthToken
import com.sourabh.fragnavdemo.persistence.AccountPropertiesDao
import com.sourabh.fragnavdemo.persistence.AuthTokenDao
import com.sourabh.fragnavdemo.repository.NetworkBoundResource
import com.sourabh.fragnavdemo.session.SessionManager
import com.sourabh.fragnavdemo.ui.DataState
import com.sourabh.fragnavdemo.ui.Response
import com.sourabh.fragnavdemo.ui.ResponseType
import com.sourabh.fragnavdemo.ui.auth.state.AuthViewState
import com.sourabh.fragnavdemo.ui.auth.state.LoginFields
import com.sourabh.fragnavdemo.ui.auth.state.RegistrationFields
import com.sourabh.fragnavdemo.util.AbsentLiveData
import com.sourabh.fragnavdemo.util.ApiSuccessResponse
import com.sourabh.fragnavdemo.util.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.sourabh.fragnavdemo.util.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.sourabh.fragnavdemo.util.GenericApiResponse
import com.sourabh.fragnavdemo.util.PreferenceKeys
import com.sourabh.fragnavdemo.util.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharedPrefsEditor: SharedPreferences.Editor
)
{

    private val TAG = "AppDebug"

    private var repositoryJob : Job? = null

    fun attemptLogin(email : String, password : String) : LiveData<DataState<AuthViewState>>{
        val loginFieldErros = LoginFields(email, password).isValidForLogin()
        if(!loginFieldErros.equals(LoginFields.LoginError.none())){
            return retrunErrorMessage(loginFieldErros,ResponseType.Dialog())
        }
        return object : NetworkBoundResource<LoginResponse, AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            true
        ){
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Log.d(TAG,"handleApiSuccessResponse :  ${response}")
                if(response.body.response.equals(GENERIC_AUTH_ERROR)){
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                accountPropertiesDao.insertOrReplace(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )

                // will return -1 if failure
                val result1 = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )
                if(result1 < 0){
                     onCompleteJob(DataState.error(
                        Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                    ))
                    return
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                return openApiAuthService.login(email,password)
            }

            override fun setJob(jon: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

            //not applicable in this case
            override suspend fun createCacheRequestAndReturn() {
                TODO("Not yet implemented")
            }

        }.asLiveData()
    }

    fun attemptRegistration(email : String, username : String, password: String
                            , confirm_password : String) : LiveData<DataState<AuthViewState>>{
        val registrationFieldErrors = RegistrationFields(email, username, password, confirm_password)
            .isValidForRegistration()
        if(!registrationFieldErrors.equals(RegistrationFields.RegistrationError.none())){
            return retrunErrorMessage(registrationFieldErrors,ResponseType.Dialog())
        }

        return object : NetworkBoundResource<RegistrationResponse,AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                Log.d(TAG,"handleApiSuccessResponse :  ${response}")
                if(response.body.response.equals(GENERIC_AUTH_ERROR)){
                    return onErrorReturn(response.body.errorMessage, true, false)
                }
                accountPropertiesDao.insertOrReplace(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )

                // will return -1 if failure
                val result1 = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )
                if(result1 < 0){
                    onCompleteJob(DataState.error(
                        Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                    ))
                    return
                }

                saveAuthenticatedUserToPrefs(email)
                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                    return openApiAuthService.register(email,username,password,confirm_password)
            }

            override fun setJob(jon: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

            // not applicable in this case
            override suspend fun createCacheRequestAndReturn() {
                TODO("Not yet implemented")
            }

        }. asLiveData()
    }

    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>>{

        val previousAuthUserEmail: String? = sharedPreferences.
        getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if(previousAuthUserEmail.isNullOrBlank()){
            Log.d(TAG, "checkPreviousAuthUser: No previously authenticated user found.")
            return returnNoTokenFound()
        }
        else{
            return object: NetworkBoundResource<Void, AuthViewState>(
                sessionManager.isConnectedToTheInternet(),
                false,
            ){

                override suspend fun createCacheRequestAndReturn() {
                    accountPropertiesDao.searchByEmail(previousAuthUserEmail).let { accountProperties ->
                        Log.d(TAG, "createCacheRequestAndReturn: searching for token... account properties: ${accountProperties}")

                        accountProperties?.let {
                            if(accountProperties.pk > -1){
                                authTokenDao.searchByPk(accountProperties.pk).let { authToken ->
                                    if(authToken != null){
                                        if(authToken.token != null){
                                            onCompleteJob(
                                                DataState.data(
                                                    AuthViewState(authToken = authToken)
                                                )
                                            )
                                            return
                                        }
                                    }
                                }
                            }
                        }
                        Log.d(TAG, "createCacheRequestAndReturn: AuthToken not found...")
                        onCompleteJob(
                            DataState.data(
                                null,
                                Response(
                                    RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                    ResponseType.None()
                                )
                            )
                        )
                    }
                }

                // not used in this case
                override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {
                }

                // not used in this case
                override fun createCall(): LiveData<GenericApiResponse<Void>> {
                    return AbsentLiveData.create()
                }

                override fun setJob(job: Job) {

                }


            }.asLiveData()
        }
    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    data = null,
                    response = Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None())
                )
            }
        }
    }

    private fun saveAuthenticatedUserToPrefs(email: String){
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

    private fun retrunErrorMessage(loginFieldErros: String, dialog: ResponseType.Dialog):
            LiveData<DataState<AuthViewState>>
    {
      return object : LiveData<DataState<AuthViewState>>(){
          override fun onActive() {
              super.onActive()
              value = DataState.error(
                  Response(message = loginFieldErros,responseType = dialog)
              )
          }
      }
    }

    fun cancelActiveJob() {
        Log.d(TAG,"AuthRepository : Cancelling on going job")
        repositoryJob?.cancel()
    }



}
