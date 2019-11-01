package com.vikaspachdha.ping

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

class PingService : Service() {
    private var m_ip: String = ""
    private var notificationManager: NotificationManager? = null
    var stopPing = false
    var pingThread = Thread(
        Runnable {
            var count = 0
            while (!this.stopPing) {
                Thread.sleep(1000)
                println("Pinging ${++count}")
            }
        }
    )

    private fun buildNotification():Notification {
        val notificationID = 101
        var b = Notification.Builder(applicationContext, getString(R.string.channel_id))
        b.setContentTitle("Pinging")
        b.setContentText("Pinging IP $m_ip")
        b.setSmallIcon(android.R.drawable.ic_dialog_alert)
        b.setChannelId(getString(R.string.channel_id))
        return b.build()
        //notificationManager?.notify(notificationID, n)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        m_ip = when(intent?.hasExtra(getString(R.string.data_ip_address))!!) {
            true -> intent.getStringExtra(getString(R.string.data_ip_address))
            false -> "Invalid IP"
        }
        val notification = buildNotification()
        startForeground(541, notification)
        this.pingThread.start()
        return START_STICKY
    }

    override fun onDestroy() {
        this.stopPing = true
        this.pingThread.join()
        super.onDestroy()
    }


}
