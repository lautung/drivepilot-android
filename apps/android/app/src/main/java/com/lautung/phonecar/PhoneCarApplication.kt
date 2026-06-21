package com.lautung.phonecar

import android.app.Application

class PhoneCarApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
