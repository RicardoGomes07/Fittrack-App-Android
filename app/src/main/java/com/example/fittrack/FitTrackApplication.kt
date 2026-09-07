package com.example.fittrack

import android.app.Application
import com.example.fittrack.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FitTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FitTrackApplication)
            modules(appModule)
        }
    }
}
