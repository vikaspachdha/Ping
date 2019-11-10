package com.vikaspachdha.ping

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import java.net.Inet4Address
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Vibrator
import android.util.Log

const val gLogTag = "PingService"

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

    private var mNotificationManager: NotificationManager? = null
    private var mState = State.IDLE
    private var mIp: String = ""
    private var mStopPing = false
    private var mPingThread = Thread { doPing() }
    private var mMissedCount = 0

    private var mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == actionPing) {
                Log.d(gLogTag, "Received ping")
                LocalBroadcastManager.getInstance(applicationContext)
                    .sendBroadcast(Intent(actionPong))
            }
        }
    }

    private fun doPing() {
        val ip = Inet4Address.getByName(mIp)
        while (!mStopPing) {
            if (ip.isReachable(500)) {
                Log.d(gLogTag, "Connected")
                setState(State.CONNECTED)
                mMissedCount = 0
            } else {
                Log.d(gLogTag, "Ping failed: $mMissedCount")
                if (++mMissedCount > resources.getInteger(R.integer.panic_threshold)) {
                    Log.d(gLogTag, "Disconnected")
                    setState(State.DISCONNECTED)
                }
            }
            Thread.sleep(1000)
        }
        setState(State.IDLE)
    }

    private fun setState(newState: State) {
        if (newState != this.mState) {
            synchronized(this) {
                this.mState = newState
            }
            Log.d(gLogTag, "State changed $mState")
            if (this.mState == State.DISCONNECTED) {
                goPanic()
            }
            updateNotification()
        }
    }

    private fun buildNotification(): Notification {
        var b = Notification.Builder(applicationContext, getString(R.string.channel_id))
        b.setContentTitle("Pinging $mIp")
        b.setContentText(notificationText())
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
        mIp = "Invalid IP"
        if (ip != null) {
            mIp = ip
        }
        val notification = buildNotification()
        startForeground(resources.getInteger(R.integer.notification_id), notification)
        this.mPingThread.start()
        return START_STICKY
    }

    override fun onDestroy() {
        this.mStopPing = true
        this.mPingThread.join()
        super.onDestroy()
    }

    private fun showUI() {
        val dialogIntent = Intent(this, MainActivity::class.java)
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(dialogIntent)
    }

    private fun soundAlarm() {
//        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        if (vibrator.hasVibrator()) {
//            vibrator.vibrate(500) // for 500 ms
//        }
    }

    private fun goPanic() {
        Log.i(gLogTag, "Going panic")
        showUI()
        soundAlarm()
    }

    private fun updateNotification() {
        Log.d(gLogTag, "Updating notification")
        val notification = buildNotification()
        notification.flags = Notification.FLAG_ONGOING_EVENT
        mNotificationManager?.notify(resources.getInteger(R.integer.notification_id), notification)
    }

    private fun notificationText(): String {
        val statusStr = when (mState) {
            State.CONNECTED -> "Connected"
            State.DISCONNECTED -> "PANIC"
            else -> "Idle"
        }
        return "Current state: $statusStr"
    }
}
