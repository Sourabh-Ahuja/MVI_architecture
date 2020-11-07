package com.sourabh.fragnavdemo.session

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sourabh.fragnavdemo.models.AuthToken
import com.sourabh.fragnavdemo.persistence.AuthTokenDao
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject
constructor(
 val authTokenDao : AuthTokenDao,
 val application: Application
)
{
      private val TAG = "AppDebug"

      private val _cachedToken = MutableLiveData<AuthToken>()

      val cachedToken: LiveData<AuthToken>
       get() = _cachedToken

      fun login(newValue: AuthToken){
        setValue(newValue)
      }

      private fun setValue(newValue: AuthToken?) {
       // we are running coroutine on main thread, because live data value must be set on main thread
       GlobalScope.launch(Dispatchers.Main) {
        if (_cachedToken.value != newValue) {
         _cachedToken.value = newValue
        }
       }
      }

 fun logout(){
  Log.d(TAG, "logout: ")


  CoroutineScope(Dispatchers.IO).launch{
   var errorMessage: String? = null
   try{
    _cachedToken.value!!.account_pk?.let { authTokenDao.nullifyToken(it)
    } ?: throw CancellationException("Token Error. Logging out user.")
   }catch (e: CancellationException) {
    Log.e(TAG, "logout: ${e.message}")
    errorMessage = e.message
   }
   catch (e: Exception) {
    Log.e(TAG, "logout: ${e.message}")
    errorMessage = errorMessage + "\n" + e.message
   }
   finally {
    errorMessage?.let{
     Log.e(TAG, "logout: ${errorMessage}" )
    }
    Log.d(TAG, "logout: finally")
    setValue(null)
   }
  }
 }

 fun isConnectedToTheInternet(): Boolean{
  val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  try{
   return cm.activeNetworkInfo.isConnected
  }catch (e: Exception){
   Log.e(TAG, "isConnectedToTheInternet: ${e.message}")
  }
  return false
 }
}