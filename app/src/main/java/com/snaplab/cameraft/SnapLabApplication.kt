package com.snaplab.cameraft

import android.app.Application

class SnapLabApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SnapLabApplication
            private set
    }
}
