package demo.com.br.googleio

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.widget.Toast
import dji.common.error.DJIError
import dji.common.error.DJISDKError
import dji.sdk.camera.Camera
import dji.sdk.products.Aircraft
import dji.sdk.base.BaseComponent
import dji.sdk.base.BaseProduct
import dji.sdk.products.HandHeld
import dji.sdk.sdkmanager.DJISDKManager

class GoogleIOApplication : Application() {

    private var mDJISDKManagerCallback: DJISDKManager.SDKManagerCallback? = null
    private var mDJIBaseProductListener: BaseProduct.BaseProductListener? = null
    private var mDJIComponentListener: BaseComponent.ComponentListener? = null

    private var instance: Application? = null
    private lateinit var mHandler: Handler

    private val updateRunnable = Runnable {
        val intent = Intent(FLAG_CONNECTION_CHANGE)
        applicationContext!!.sendBroadcast(intent)
    }

    fun setContext(application: Application) {
        instance = application
    }

    override fun getApplicationContext(): Context? {
        return instance
    }

    override fun onCreate() {
        super.onCreate()
        mHandler = Handler(Looper.getMainLooper())

        mDJIComponentListener = BaseComponent.ComponentListener { notifyStatusChange() }

        mDJIBaseProductListener = object : BaseProduct.BaseProductListener {
            //p0 = key, p1 = oldProduct   p2 = newProduct
            override fun onComponentChange(p0: BaseProduct.ComponentKey?, p1: BaseComponent?, p2: BaseComponent?) {
                p2?.setComponentListener(mDJIComponentListener)
                notifyStatusChange()
            }

            override fun onConnectivityChange(isConnected: Boolean) {
                notifyStatusChange()
            }
        }

        mDJISDKManagerCallback = object : DJISDKManager.SDKManagerCallback {

            //Listens to the SDK registration result
            override fun onRegister(error: DJIError) {
                if (error === DJISDKError.REGISTRATION_SUCCESS) {
                    val handler = Handler(Looper.getMainLooper())
                    handler.post { Toast.makeText(applicationContext,"Registro realizado com sucesso!",Toast.LENGTH_LONG).show() }
                    DJISDKManager.getInstance().startConnectionToProduct()
                } else {
                    val handler = Handler(Looper.getMainLooper())
                    handler.post { Toast.makeText(applicationContext,"Registro falhou!",Toast.LENGTH_LONG).show() }
                }
            }
            //p0 = oldProduct   p1 = newProduct
            override fun onProductChange(p0: BaseProduct?, p1: BaseProduct?) {
                mProduct = p1
                if (mProduct != null) {
                    mProduct!!.setBaseProductListener(mDJIBaseProductListener)
                }
                notifyStatusChange()
            }
        }

        //Check the permissions before registering the application for android system 6.0 above.
        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext!!, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionCheck2 = ContextCompat.checkSelfPermission(applicationContext!!, android.Manifest.permission.READ_PHONE_STATE)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissionCheck == 0 && permissionCheck2 == 0) {
            //This is used to start SDK services and initiate SDK.
            DJISDKManager.getInstance().registerApp(applicationContext, mDJISDKManagerCallback)
            val handler = Handler(Looper.getMainLooper())
            handler.post { Toast.makeText(applicationContext,"Registrando!",Toast.LENGTH_LONG).show() }
        } else {
            val handler = Handler(Looper.getMainLooper())
            handler.post { Toast.makeText(applicationContext,"Preciso de permissões!",Toast.LENGTH_LONG).show() }
        }
    }

    private fun notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable)
        mHandler.postDelayed(updateRunnable, 500)
    }

    companion object {

        val FLAG_CONNECTION_CHANGE = "drone_connection_change"

        private var mProduct: BaseProduct? = null

        val productInstance: BaseProduct?
            @Synchronized get() {
                if (null == mProduct) {
                    mProduct = DJISDKManager.getInstance().product
                }
                return mProduct
            }

        val cameraInstance: Camera?
            @Synchronized get() {
                if (productInstance == null) return null
                var camera: Camera? = null
                if (productInstance is Aircraft) {
                    camera = (productInstance as Aircraft).camera

                } else if (productInstance is HandHeld) {
                    camera = (productInstance as HandHeld).camera
                }
                return camera
            }
    }
}