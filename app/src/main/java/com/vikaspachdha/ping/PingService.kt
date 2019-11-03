package com.vikaspachdha.ping

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import java.net.Inet4Address
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.BroadcastReceiver
import android.content.IntentFilter


class PingService : Service() {

    enum class State {
        IDLE,
        CONNECTED,
        DISCONNECTED
    }

    companion object {
        val actionPing = PingService::class.java.name + ".PING"
        val actionPong = PingService::class.java.name + ".PONG"
    }


    private var m_state = State.IDLE
    private var m_ip: String = ""
    private var stopPing = false
    private var pingThread = Thread{doPing()}

    private var mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == actionPing) {
                println("Received ping")
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(actionPong))
            }
        }
    }


    private fun doPing() {
        val ip = Inet4Address.getByName(m_ip)
        var count = 0
        while (!stopPing) {
            println("Checking IP ${count++}")
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

    override fun onCreate() {
        val manager = LocalBroadcastManager.getInstance(applicationContext)
        manager.registerReceiver(mReceiver, IntentFilter(actionPing))
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ip = intent?.getStringExtra(getString(R.string.data_ip_address))
        m_ip = "Invalid IP"
        if (ip != null) {
            m_ip = ip
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
