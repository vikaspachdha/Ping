package com.vikaspachdha.ping

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.Date
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import android.text.method.ScrollingMovementMethod

class MainActivity : AppCompatActivity() {
    private var notificationManager: NotificationManager? = null
    private var mPlayer = MediaPlayer()
    private var mPingIntent = Intent()


    private fun log(msg: String) {
        val timeStamp = SimpleDateFormat("dd--HH:mm:ss:SSS").format(Date())
        logText.append("$timeStamp $msg\n")
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

    private fun playAlarm() {
        mPlayer.start()
    }

    private fun stopAlarm() {
        mPlayer.stop()
        mPlayer = MediaPlayer.create(this, R.raw.alarm)
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

        logText.movementMethod = ScrollingMovementMethod()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(getString(R.string.channel_id), "Notify Ping", "Connectivity status change")

        mPlayer = MediaPlayer.create(this, R.raw.alarm)
        mPingIntent = Intent(this, PingService::class.java)

        monitorSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startPinging()
            } else if (!isChecked) {
                stopPinging()
            }
        }
    }
}
