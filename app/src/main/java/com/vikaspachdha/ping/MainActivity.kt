package com.vikaspachdha.ping

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.text.method.ScrollingMovementMethod
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.widget.CompoundButton
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager




class MainActivity : AppCompatActivity() {
    private var notificationManager: NotificationManager? = null
    private var mPingIntent = Intent()
    private var mServiceRunning = false

    private fun log(msg: String) {
        PingLogger.addLog(msg)
    }

    private var mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // here you receive the response from the service
            if (intent.action == PingService.msgPong) {
                log("Got Pong")
                mServiceRunning = true
                updateServiceRunningStatus()
            }
        }
    }

    private var mMonitorChangedListener: CompoundButton.OnCheckedChangeListener = object : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(view: CompoundButton, isChecked: Boolean) {
            if (isChecked) {
                startPinging()
            } else if (!isChecked) {
                stopPinging()
            }
        }
    }

    private fun createNotificationChannel(id: String, name: String, description: String) {

        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(id, name, importance)

        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern =
            longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationManager?.createNotificationChannel(channel)
    }

    private fun startPinging() {
        val ip = ipText.text.toString()
        if (!isValidIP(ip)) {
            showToast(getString(R.string.toast_invalid_ip))
            return
        }
        log(getString(R.string.toast_ping_start))
        showToast(getString(R.string.toast_ping_start))
        mPingIntent.putExtra(getString(R.string.data_ip_address), ip)
        startService(mPingIntent)
    }

    private fun stopPinging() {
        log(getString(R.string.toast_ping_stop))
        showToast(getString(R.string.toast_ping_stop))
        stopService(mPingIntent)
    }

    private fun showToast(msg: String) {
        val toast = Toast.makeText(this, msg, Toast.LENGTH_LONG)
        toast.show()
    }


    fun isValidIP(str:String): Boolean {
        val tokens = str.split('.')
        if (tokens.size == 4) {
            return !(tokens.any{(it.toInt() < 1 || it.toInt() > 255)})
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PingLogger.LogView =  logText
        PingLogger.LogSavePath = this.filesDir.absolutePath

        loadSettings()

        logText.movementMethod = ScrollingMovementMethod()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(getString(R.string.channel_id), "Notify Ping", "Connectivity status change")

        mPingIntent = Intent(this, PingService::class.java)

        monitorSwitch.setOnCheckedChangeListener(mMonitorChangedListener)

        clearLogbtn.setOnClickListener {
            PingLogger.clear()
        }
    }

    override fun onStart() {
        val manager = LocalBroadcastManager.getInstance(applicationContext)
        manager.registerReceiver(mReceiver, IntentFilter(PingService.msgPong))
        log("Sending Ping")
        manager.sendBroadcast(Intent(PingService.msgPing))
        super.onStart()
    }

    private fun updateServiceRunningStatus() {
        monitorSwitch.setOnCheckedChangeListener(null)
        monitorSwitch.isChecked = mServiceRunning
        monitorSwitch.setOnCheckedChangeListener(mMonitorChangedListener)
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver)
        saveSettings()
        PingLogger.saveLogs()
        super.onStop()
    }

    private fun saveSettings() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putString(getString(R.string.data_ip_address), ipText.text.toString())
            commit()
        }
    }

    private fun loadSettings() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        findViewById<TextView>(R.id.ipText).text = sharedPref.getString(getString(R.string.data_ip_address), "192.168.1.1")
    }
}
