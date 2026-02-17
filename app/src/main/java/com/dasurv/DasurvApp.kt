package com.dasurv

import android.app.Application
import com.dasurv.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DasurvApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
