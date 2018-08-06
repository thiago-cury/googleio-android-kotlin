package demo.com.br.googleio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dji.common.flightcontroller.FlightControllerState
import dji.common.util.CommonCallbacks
import dji.sdk.flightcontroller.FlightController
import dji.sdk.products.Aircraft
import org.jetbrains.anko.toast

class MissionActivity : AppCompatActivity() {

    private lateinit var btTakeOff : Button
    private lateinit var btMotorsOff : Button
    private lateinit var btMotorsOn : Button
    private lateinit var btLEDsOn : Button
    private lateinit var btLEDsOff : Button

    private var mFlightController : FlightController? = null

    private var droneLat : Double = 0.0
    private var droneLng : Double = 0.0

    //Firebase
    private var dbRef: DatabaseReference? = null
    private var db: FirebaseDatabase? = null

    //Mission
    private var mission: Mission? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission)

        //Refs
        btTakeOff = findViewById(R.id.bt_takeoff)
        btMotorsOff = findViewById(R.id.bt_motors_off)
        btMotorsOn = findViewById(R.id.bt_motors_on)
        btLEDsOn = findViewById(R.id.bt_leds_on)
        btLEDsOff = findViewById(R.id.bt_leds_off)

        btTakeOff.setOnClickListener(){ takeOff() }
        btMotorsOff.setOnClickListener(){ turnMotorsOff() }
        btMotorsOn.setOnClickListener(){ turnMotorsOn() }
        btLEDsOn.setOnClickListener() { turnLEDsOn() }
        btLEDsOff.setOnClickListener() { turnLEDsOff() }

        val filter = IntentFilter()
        filter.addAction(GoogleIOApplication.FLAG_CONNECTION_CHANGE)
        registerReceiver(mReceiver, filter)

        //Firebase
        db = FirebaseDatabase.getInstance()
        dbRef = db!!.reference!!.child("missions")

        mission = Mission()

    }

    override fun onResume() {
        super.onResume()
        onProductChange()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }

    protected  var mReceiver: BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            onProductChange()
        }
    }

    private fun onProductChange(){
        initFlightController()
        initOnBoardController()
    }

    private fun initFlightController(){
        val product = GoogleIOApplication.productInstance
        if(product != null && product!!.isConnected) {
            if(product is Aircraft){
                mFlightController = (product as Aircraft).flightController
            }

            toast("modelo: ${product.model}")

            mFlightController?.setStateCallback(
                    FlightControllerState.Callback {

                        if(droneLat != 0.0 && droneLng != 0.0) {
                            if (!"%.4f".format(it.aircraftLocation.latitude).equals("%.4f".format(droneLat)) ||
                                    !"%.4f".format(it.aircraftLocation.longitude).equals("%.4f".format(droneLng))) {

                                //Push to Firebase
                                mission?.lat = droneLat
                                mission?.lng = droneLng
                                dbRef!!.setValue(mission)
                            }
                        }
                        droneLat = it.aircraftLocation.latitude
                        droneLng = it.aircraftLocation.longitude

                    }
            )
        }
    }

    private fun takeOff(){
        val product = GoogleIOApplication.productInstance
        if(product != null && product!!.isConnected) {
            mFlightController = (product as Aircraft).flightController
            mFlightController?.startTakeoff { }
        }
    }

    private fun turnMotorsOff(){
        val product = GoogleIOApplication.productInstance
        if(product != null && product!!.isConnected) {
            mFlightController = (product as Aircraft).flightController
            mFlightController?.turnOffMotors { }
        }
    }

    private fun turnMotorsOn(){
        val product = GoogleIOApplication.productInstance
        if(product != null && product!!.isConnected) {
            mFlightController = (product as Aircraft).flightController
            mFlightController?.turnOnMotors {  }
        }
    }

    private fun turnLEDsOn(){
        val product = GoogleIOApplication.productInstance
        if(product != null && product!!.isConnected) {
            mFlightController = (product as Aircraft).flightController
            mFlightController?.setLEDsEnabled(true, CommonCallbacks.CompletionCallback {  })
        }
    }

    private fun turnLEDsOff(){
        val product = GoogleIOApplication.productInstance
        if(product != null && product!!.isConnected) {
            mFlightController = (product as Aircraft).flightController
            mFlightController?.setLEDsEnabled(false, CommonCallbacks.CompletionCallback {  })
        }
    }

    private fun initOnBoardController(){
        val product = GoogleIOApplication.productInstance
        if(product != null && product!!.isConnected) {
            if(product is Aircraft){
                mFlightController = (product as Aircraft).flightController
            }


            if(mFlightController?.isOnboardSDKDeviceAvailable!!){
                toast("está disponível!")

                mFlightController?.setOnboardSDKDeviceDataCallback({
                    toast("retorno: ${it.toString()}")

                    //Push to Firebase
                    dbRef!!.child("laser1").setValue(it.toString())
                })

                mFlightController?.setOnboardSDKDeviceDataCallback (
                        FlightController.OnboardSDKDeviceDataCallback {
                            toast("retorno 1: ${it.toString()}")
                            dbRef!!.child("laser2").setValue(it.toString())
                        })

                mFlightController?.setOnboardSDKDeviceDataCallback(
                        FlightController.OnboardSDKDeviceDataCallback { p0 ->
                            toast("retorno 2 p0: ${p0.toString()}")
                            dbRef!!.child("laser3").setValue(p0.toString())
                        })
            }
        }
    }
}