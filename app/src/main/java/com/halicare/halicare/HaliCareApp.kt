package com.halicare.halicare

import android.app.Application
import com.halicare.halicare.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HaliCareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HaliCareApp)
            modules(appModules)
        }
    }
}