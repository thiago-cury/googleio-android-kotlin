package demo.com.br.googleio

import android.app.Application
import android.content.Context
import com.secneo.sdk.Helper

class MApplication : Application {

    constructor() : super()

    private var googleIO: GoogleIOApplication? = null

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        Helper.install(this@MApplication)

        if (googleIO == null) {
            googleIO = GoogleIOApplication()
            googleIO!!.setContext(this)
        }
    }

    override fun onCreate() {
        super.onCreate()

        googleIO!!.onCreate()
    }
}