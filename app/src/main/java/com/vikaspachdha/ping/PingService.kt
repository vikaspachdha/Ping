package com.vikaspachdha.ping

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.net.Inet4Address

class PingService : Service() {

    enum class State {
        IDLE,
        CONNECTED,
        DISCONNECTED
    }

    private var m_state = State.IDLE
    private var m_ip: String = ""
    var stopPing = false
    var pingThread = Thread{doPing()}


    private fun doPing() {
        val ip = Inet4Address.getByName(m_ip)
        while (!stopPing) {
            setState(when(ip.isReachable(500)){
                true -> State.CONNECTED
                false -> State.DISCONNECTED
            })
            Thread.sleep(1000)
        }
        setState(State.IDLE)
    }

    private fun setState(newState: State) {
        if (newState != this.m_state) {
            synchronized(this) {
                this.m_state = newState
            }
            println("State changed $m_state")
        }
    }

    private fun buildNotification():Notification {
        var b = Notification.Builder(applicationContext, getString(R.string.channel_id))
        b.setContentTitle("Pinging")
        b.setContentText("Pinging IP $m_ip")
        b.setSmallIcon(android.R.drawable.ic_dialog_alert)
        b.setChannelId(getString(R.string.channel_id))
        return b.build()
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
