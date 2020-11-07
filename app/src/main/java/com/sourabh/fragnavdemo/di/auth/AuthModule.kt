package com.sourabh.fragnavdemo.di.auth

import android.content.SharedPreferences
import com.sourabh.fragnavdemo.api.auth.OpenApiAuthService
import com.sourabh.fragnavdemo.persistence.AccountPropertiesDao
import com.sourabh.fragnavdemo.persistence.AuthTokenDao
import com.sourabh.fragnavdemo.repository.auth.AuthRepository
import com.sourabh.fragnavdemo.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class AuthModule{

    @AuthScope
    @Provides
    fun provideOpenApiAuthService(retrofirBuilder : Retrofit.Builder): OpenApiAuthService {
        return retrofirBuilder
            .build()
            .create(OpenApiAuthService::class.java)
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        openApiAuthService: OpenApiAuthService,
    ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            openApiAuthService,
            sessionManager,
        )
    }

}