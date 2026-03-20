package com.SzpontCompany.check

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize

class CheckApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
    }
}