package com.example.ridepassenger2

import android.app.Application
import com.example.ridepassenger2.data.notifications.PushNotifications
import com.example.ridepassenger2.data.remote.RidaApiClient

class RidaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RidaApiClient.init(this)
        PushNotifications.start(this)
    }
}
