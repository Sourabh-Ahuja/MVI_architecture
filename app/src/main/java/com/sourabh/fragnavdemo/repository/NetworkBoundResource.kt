package com.sourabh.fragnavdemo.repository

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.sourabh.fragnavdemo.ui.DataState
import com.sourabh.fragnavdemo.ui.Response
import com.sourabh.fragnavdemo.ui.ResponseType
import com.sourabh.fragnavdemo.util.*
import com.sourabh.fragnavdemo.util.Constants.Companion.NETWORK_TIMEOUT
import com.sourabh.fragnavdemo.util.Constants.Companion.TESTING_NETWORK_DELAY
import com.sourabh.fragnavdemo.util.ErrorHandling.Companion.ERROR_CHECK_NETWORK_CONNECTION
import com.sourabh.fragnavdemo.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.sourabh.fragnavdemo.util.ErrorHandling.Companion.UNABLE_TODO_OPERATION_WO_INTERNET
import com.sourabh.fragnavdemo.util.ErrorHandling.Companion.UNABLE_TO_RESOLVE_HOST
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

abstract class NetworkBoundResource<ResponseObject, ViewStateType>(
    isNetworkAvailable : Boolean
) {
    private val TAG = "AppDebug"

    protected val result = MediatorLiveData<DataState<ViewStateType>>()

    protected lateinit var job : CompletableJob
    protected lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initNewJob())
        setValue(DataState.loading(isLoading = true, cachedData = null ))

        if(isNetworkAvailable){
            coroutineScope.launch {
                delay(TESTING_NETWORK_DELAY)

                withContext(Main){
                    // we using mediator live data we can call on mainthread
                    val apiResponse = createCall()
                    result.addSource(apiResponse) {response ->
                        result.removeSource(apiResponse)

                        coroutineScope.launch {
                            handleNetworkResponse(response)
                        }
                    }
                }
            }
            GlobalScope.launch(IO) {
                delay(NETWORK_TIMEOUT)
                if(!job.isCompleted){
                    Log.d(TAG,"NetworkBoundResource : Network Job Timeout")
                    job.cancel(CancellationException(UNABLE_TO_RESOLVE_HOST))
                }
                job.cancel()
            }

        } else {
            onErrorReturn(UNABLE_TODO_OPERATION_WO_INTERNET,true,false)
        }
    }

    suspend fun handleNetworkResponse(response: GenericApiResponse<ResponseObject>?) {
        when(response){
            is ApiSuccessResponse ->{
                handleApiSuccessResponse(response)
            }
            is ApiErrorResponse ->{
                Log.e(TAG, "NetworkBoundResource: ${response.errorMessage}")
                onErrorReturn(response.errorMessage, true, false)
            }
            is ApiEmptyResponse ->{
                Log.e(TAG, "NetworkBoundResource: Request returned NOTHING (HTTP 204).")
                onErrorReturn("HTTP 204. Returned NOTHING.", true, false)
            }
        }
    }

    fun onCompleteJob(dateState : DataState<ViewStateType>){
        GlobalScope.launch(Main) {
            job.complete()
            setValue(dateState)
        }
    }

    private fun setValue(dateState: DataState<ViewStateType>) {
        result.value = dateState
    }

    fun onErrorReturn(errorMessgae : String? , shouldUseDialog : Boolean, shouldUseToast : Boolean) {
        var msg = errorMessgae
        var useDialog = shouldUseDialog
        var responseType : ResponseType = ResponseType.None()

        if(msg == null){
            msg = ERROR_UNKNOWN
        } else if (ErrorHandling.isNetworkError(msg)){
            msg = ERROR_CHECK_NETWORK_CONNECTION
            useDialog = false
        }

        if(shouldUseToast){
            responseType = ResponseType.Toast()
        }

        if(useDialog){
            responseType = ResponseType.Dialog()
        }

        onCompleteJob(DataState.error(
            response = Response(
                message = msg,
                responseType = responseType
            )
        ))
    }

    @OptIn(InternalCoroutinesApi::class)
    private fun initNewJob() : Job{
        Log.d(TAG,"initJob : called")
        job = Job()
        job.invokeOnCompletion(onCancelling = true, invokeImmediately = true, handler = object :
            CompletionHandler{

            override fun invoke(cause: Throwable?) {
                if(job.isCancelled){
                    Log.e(TAG,"NetworkBoundResource : Job has been cancelled")
                    cause?.let {
                        onErrorReturn( it.message, false, true)
                    }?: onErrorReturn( ERROR_UNKNOWN, false, true)

                } else if (job.isCompleted){
                    Log.e(TAG,"NetworkBoundResource : Job has been completed")
                     // Do nothing, should be handled alraady

                }
            }
        })
        // we are using this because if we want to cancel a job then we just to need to call
        // job.cancel()
        // if we use CoroutineScope(IO), then to cancel a job we need to call coroutineScope.cancel()
        // which will cancel all the job on IO. So to cancel only on job we are using this way
        coroutineScope = CoroutineScope(IO + job)
        return job
    }

    fun asLiveData() = result as LiveData<DataState<ViewStateType>>

    abstract suspend fun handleApiSuccessResponse(response : ApiSuccessResponse<ResponseObject>)

    abstract fun createCall() : LiveData<GenericApiResponse<ResponseObject>>

    abstract fun setJob(jon : Job)
}