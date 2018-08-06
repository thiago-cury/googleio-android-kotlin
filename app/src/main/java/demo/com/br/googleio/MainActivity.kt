package demo.com.br.googleio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Button
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btOpen : Button

    private var REQUIRED_PERMISSION_LIST: MutableList<String> = mutableListOf<String>(
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE)

    private var missingPermission: MutableList<String> = mutableListOf<String>()
    private val REQUEST_PERMISSION_CODE: Int = 12345

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) checkAndRequestPermissions()

        //Refs
        btOpen = findViewById(R.id.bt_open)

        btOpen.setOnClickListener() { startActivity<MissionActivity>() }

    }

    private fun checkAndRequestPermissions() {
        for (eachPermission in REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission)
            }
        }

        if (!missingPermission.isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast("Preciso de permissões")
            ActivityCompat.requestPermissions(this, missingPermission.toTypedArray(), REQUEST_PERMISSION_CODE)
        }
    }

    private fun showToast(toastMsg: String) {
        var handler: Handler = Handler(Looper.getMainLooper())
        handler.post { kotlin.run { longToast(toastMsg) } }
    }
}