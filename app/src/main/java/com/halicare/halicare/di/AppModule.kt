package com.halicare.halicare.di

import com.halicare.halicare.api.ApiInterface
import com.halicare.halicare.api.AuthTokenProvider
import com.halicare.halicare.repository.AuthRepository
import com.halicare.halicare.data.LocationRepository
import com.halicare.halicare.data.SettingsRepository
import com.halicare.halicare.data.SettingsRepositoryImpl
import com.halicare.halicare.repository.ClinicRepository
import com.halicare.halicare.repository.CounselingCenterRepository
import com.halicare.halicare.viewModel.LocationViewModel
import com.halicare.halicare.viewModel.NavigationViewModel
import com.halicare.halicare.viewModel.AuthViewModel
import com.halicare.halicare.viewModel.ClinicDetailViewModel
import com.halicare.halicare.viewModel.ClinicViewModel
import com.halicare.halicare.viewModel.CounselingCenterViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val networkModule = module {
    single { AuthTokenProvider() }


    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        val tokenProvider: AuthTokenProvider = get()
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val token = tokenProvider.token
                if (!token.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", "Token $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()
    }


    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("https://halicare-7bfc32637910.herokuapp.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
    }


    single<ApiInterface> {
        get<Retrofit>().create(ApiInterface::class.java)
    }
}


val repositoryModule = module {
    single<ClinicRepository> { ClinicRepository(get()) }
    single { AuthRepository(get(), get()) }
    single { CounselingCenterRepository(api = get()) }
    single { LocationRepository(androidContext()) }
    single<SettingsRepository> { SettingsRepositoryImpl(androidContext()) }
}


val viewModelModule = module {
    viewModel { ClinicViewModel(get(), androidContext()) }
    viewModel { ClinicDetailViewModel(get(),tokenProvider = get(), androidContext()) }
    viewModel { AuthViewModel(get(), get()) }
    viewModel { CounselingCenterViewModel(repository = get()) }
    viewModel { LocationViewModel(get()) }
    viewModel { NavigationViewModel(get()) }
}


val appModules = listOf(networkModule, repositoryModule, viewModelModule)

