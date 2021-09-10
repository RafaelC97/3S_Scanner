package com.example.myapplication

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.login_popup.view.*
import kotlinx.android.synthetic.main.settings_menu.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.lang.Thread.sleep
import java.util.*
import java.util.Calendar.*


private const val CAMERA_REQUEST_CODE = 101
var str: String = "0"

class MainActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var mqttClient: MqttAndroidClient

    companion object {
        const val TAG = "AndroidMqttClient"
    }

    private lateinit var codeScanner: CodeScanner
    var day = 0
    var month = 0
    var year = 0

    var UserId: String = ""
    var HouseHoldID: String = ""
    var broker: String = "0"


    var saveDay = 0
    var saveMonth = 0
    var saveYear = 0

    var strDay: String = "0"
    var strMonth: String = "0"
    var strYear: String = "0"

    var scannerMode = 1

    var productQuant = 1
    var productQuantS: String = "1"


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupPermissions()
        addItem()
        removeItem()
        codeScanner()
        pickDate()
        changeMode()
        openSettings()
        resetDate()
        askLogin()

    }

    private fun changeMode() {
        scanMode.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Fridge Scanner Mode", Toast.LENGTH_SHORT).show()
                scanMode.setText("Fridge Scanner")
                scannerMode = 0
            } else {
                Toast.makeText(this, "Trashcan Scanner Mode", Toast.LENGTH_SHORT).show()
                scanMode.setText("Trashcan Scanner")
                scannerMode = 1
            }
        }
    }

    fun askLogin() {
        if (UserId.isNullOrEmpty() || HouseHoldID.isNullOrEmpty()) {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.login_popup, null);
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("Login")
            val mAlertDialog = mBuilder.show()
            mDialogView.popupLoginButton.setOnClickListener {
                mAlertDialog.dismiss()
                UserId = mDialogView.username.text.toString()
                HouseHoldID = mDialogView.household.text.toString()
                if (UserId.isNullOrEmpty() || HouseHoldID.isNullOrEmpty()) {
                    toastLoginDismiss()
                    askLogin()
                }
                broker = "tcp://broker.emqx.io:1883"
                connect(this)
            }

        }
    }

    fun resetDate() {
        resetDate.setOnClickListener() {
            saveDay = 0
            saveMonth = 0
            saveYear = 0
            tv_expiryDate.text = "dd/MM/yyyy"
            tv_textView.text = "Scan something"

        }
    }

    fun addItem() {
        addButton.setOnClickListener {
            productQuant++
            tv_productQtt.text = productQuant.toString()
        }
    }

    fun removeItem() {
        subtractButton.setOnClickListener {

            if (productQuant > 1) productQuant--
            tv_productQtt.text = productQuant.toString()
        }
    }

    fun openSettings() {
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.settings_menu, null);
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("")
            val mAlertDialog = mBuilder.show()
            if (UserId.isNullOrEmpty() || HouseHoldID.isNullOrEmpty()) {
                mAlertDialog.bt_logout.text = "Login"
            }
            if (broker == "tcp://broker.emqx.io:1883")
                mAlertDialog.radio_broker.check(R.id.radio_emqx)
            else mAlertDialog.radio_broker.check(R.id.radio_mosquitto)
            mAlertDialog.radio_broker.setOnCheckedChangeListener { radioGroup, checkedBroker ->
                if (checkedBroker == R.id.radio_emqx) {
                    broker = "tcp://broker.emqx.io:1883"
                    connect(this)
                } else if (checkedBroker == R.id.radio_mosquitto) {
                    broker = "tcp://test.mosquitto.org:1883"
                    connect(this)
                }
            }
            mAlertDialog.tv_UserIdDrawer.text = UserId
            mAlertDialog.HouseholdIdDrawer.text = HouseHoldID
            mAlertDialog.bt_logout.setOnClickListener {
                mAlertDialog.dismiss()
                UserId = ""
                HouseHoldID = ""
                askLogin()
            }
        }
    }

    private fun codeScanner() {
        codeScanner = CodeScanner(this, scanner_view)

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                runOnUiThread {
                    tv_textView.text = it.text
                    strDay = saveDay.toString()
                    strMonth = saveMonth.toString()
                    strYear = saveYear.toString()
                    if (UserId.isNotEmpty() && HouseHoldID.isNotEmpty()) {
                        if (scannerMode == 0) {
                            productQuantS = productQuant.toString()
                            str = """"${
                                it.text.plus(' ').plus(strYear).plus(' ').plus(strMonth)
                                    .plus(' ')
                                    .plus(strDay).plus(' ').plus(UserId).plus(' ')
                                    .plus(HouseHoldID)
                                    .plus(' ').plus(productQuantS)
                            }""""
                            publish("smart-shopping/add-item-home-catalog2", str, 1, false)
                            fadeF()
                        } else {
                            str = """"${
                                it.text.plus(' ').plus(UserId).plus(' ').plus(HouseHoldID)
                                    .plus(' ')
                                    .plus(productQuantS)
                            }""""
                            publish("smart-shopping/add-item-shopping-list2", str, 1, false)
                            fadeT()
                        }
                    } else {
                        toastLoginScan()
                    }
                }
                sleep(2000)
            }
            errorCallback = ErrorCallback {
                runOnUiThread {
                    Log.e("Main", "Camera initialization error: ${it.message}")
                }
            }

        }

    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(
                        this,
                        "The camera permission must be granted to use the app",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }
    }

    private fun getDateCalendar() {
        val cal = Calendar.getInstance()
        day = cal.get(DAY_OF_MONTH)
        month = cal.get(MONTH)
        year = cal.get(YEAR)

    }

    private fun pickDate() {
        expiryButton.setOnClickListener() {
            getDateCalendar()
            DatePickerDialog(this, this, year, month, day).show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        saveDay = dayOfMonth
        saveMonth = month + 1
        saveYear = year

        tv_expiryDate.text = "$saveDay/$saveMonth/$saveYear"
    }

    fun fadeF() {
        Toast.makeText(this, "Scanned to Fridge", Toast.LENGTH_SHORT).show()
    }

    fun fadeT() {
        Toast.makeText(this, "Scanned to Trashcan", Toast.LENGTH_SHORT).show()
    }

    fun toastLoginScan() {
        Toast.makeText(this, "Login on settings to scan", Toast.LENGTH_SHORT).show()
    }

    fun toastLoginDismiss() {
        Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
    }

    //////MQTT//////
    fun connect(context: Context) {

        var serverURI = broker   //"tcp://test.mosquitto.org:1883" or "tcp://broker.emqx.io:1883"
        mqttClient = MqttAndroidClient(context, serverURI, "kotlin_client")
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(TAG, "Receive message: ${message.toString()} from topic: $topic")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
        val options = MqttConnectOptions()
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Connection failure")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    fun subscribe(topic: String, qos: Int = 1) {
        try {
            mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }


    fun unsubscribe(topic: String) {
        try {
            mqttClient.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Unsubscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to unsubscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Disconnected")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to disconnect")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

}
