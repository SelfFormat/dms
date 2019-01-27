package com.example.deadmanswitch

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : CustomStatusBarActivity(), SensorEventListener {
    private lateinit var mSensorManager: SensorManager
    private var mProximity: Sensor? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpStatusBarAppearance()
        toolbarTitle.text = resources.getString(R.string.app_name)
        supportFragmentManager.transaction(allowStateLoss = true) {
            replace(R.id.mainFrame, MainFragment.newInstance(), "MAIN")
        }
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this, mProximity)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val distance = event?.values?.get(0)?.toInt()
        if (distance == 0) {
            Log.i("Distance: ", "near")
        } else {
            Log.i("Distance: ", "far")
        }
        }

    override fun onAccuracyChanged(event: Sensor?,value: Int) {}
}