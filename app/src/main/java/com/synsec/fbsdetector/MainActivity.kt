package com.synsec.fbsdetector

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.telephony.CellIdentity
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellInfo
import android.telephony.CellInfo.CONNECTION_PRIMARY_SERVING
import android.telephony.TelephonyManager
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val telephonyManager by
        lazy { getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            if (!result.all { it.value }) {
                Toast.makeText(baseContext,
                    "Requested permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }

    var prevPci = -1

    fun getPci(cellIdentity : CellIdentity): Int {
        when(cellIdentity) {
            is CellIdentityLte -> {return cellIdentity.pci}
            is CellIdentityNr -> {return cellIdentity.pci}
        }
        return -2
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        ))
        val cellInfoView = findViewById<TextView>(R.id.CellInfoView)
        cellInfoView.movementMethod = ScrollingMovementMethod()
        val looper = mainLooper
        val handler = Handler(looper)
        val runnable = object : Runnable {
            override fun run() {
                handler.postDelayed(this, 10000)
                val callback: TelephonyManager.CellInfoCallback =
                    object : TelephonyManager.CellInfoCallback() {
                    override fun onCellInfo(cellInfo: MutableList<CellInfo>) {
                        var s = "No Cell Found\n"
                        cellInfo.forEach {
                            if (it.cellConnectionStatus == CONNECTION_PRIMARY_SERVING) {
                                Log.d("Current serving cell", it.toString())
                                s = it.cellIdentity.toString() + "\n" +
                                        it.cellSignalStrength.dbm + "\n" +
                                        it.timestampMillis.toString()
                                if (prevPci == -1) {
                                    prevPci = getPci(it.cellIdentity)
                                }
                                else {
                                    val currPci = getPci(it.cellIdentity)
                                    if (currPci == -2) {
                                        Toast.makeText(baseContext,
                                            "Downgrade Detected!", Toast.LENGTH_SHORT).show()
                                        prevPci = currPci
                                    }
                                    else if (currPci != prevPci) {
                                        Toast.makeText(baseContext,
                                            "Cell Change Detected!", Toast.LENGTH_SHORT).show()
                                        prevPci = currPci
                                    }
                                }
                            }
                        }
//                        Toast.makeText(baseContext,
//                            "Cell Info Updated", Toast.LENGTH_SHORT).show()
                        cellInfoView.text = s
                    }
                }

                telephonyManager.requestCellInfoUpdate(mainExecutor, callback)

            }
        }

        handler.post(runnable)
    }


}
