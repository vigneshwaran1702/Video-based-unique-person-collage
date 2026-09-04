package com.vignesh.uniquepersoncollage

import android.app.Application
import com.vignesh.uniquepersoncollage.util.Logger

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.i("MainApplication initialized.")
    }
}
