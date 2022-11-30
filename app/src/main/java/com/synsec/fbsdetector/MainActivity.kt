package com.synsec.fbsdetector

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.CellInfo
import android.telephony.CellInfo.CONNECTION_PRIMARY_SERVING
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyCallback.CellInfoListener
import android.telephony.TelephonyCallback.SignalStrengthsListener
import android.telephony.TelephonyManager
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val telephonyManager by
        lazy { getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        findViewById<TextView>(R.id.CellInfoView).movementMethod = ScrollingMovementMethod()
        val detectorListener: DetectorListener = object : DetectorListener() {
            override fun onCellInfoChanged(cellInfo: MutableList<CellInfo>) {
                var s = ""

                cellInfo.forEach {
                    s = if (it.cellConnectionStatus == CONNECTION_PRIMARY_SERVING) {
                        Log.d("Current serving cell", it.toString())
                        it.cellIdentity.toString() + "\n" + it.cellSignalStrength.dbm + "\n"
                    } else {
                        "No Cell Found\n"
                    }
//                    s = it.toString() + it.cellConnectionStatus
                }
                findViewById<TextView>(R.id.CellInfoView).text = s
            }

            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
//                TODO("Not yet implemented")
            }
        }
        telephonyManager.registerTelephonyCallback(mainExecutor, detectorListener)
    }

    abstract class DetectorListener : TelephonyCallback(), CellInfoListener, SignalStrengthsListener

}
