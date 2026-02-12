package com.jammit

import android.app.Application
import com.jammit.data.UnreadStore

class JammitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UnreadStore.init(this)
    }
}

