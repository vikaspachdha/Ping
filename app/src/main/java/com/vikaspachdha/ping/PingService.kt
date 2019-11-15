package com.vikaspachdha.ping

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import java.net.Inet4Address
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Vibrator
import android.util.Log
import android.os.VibrationEffect


const val gLogTag = "PingService"

class PingService : Service() {

    enum class State {
        IDLE,
        CONNECTED,
        DISCONNECTED,
        PANIC
    }

    companion object {
        val actionPing = PingService::class.java.name + ".PING"
        val actionPong = PingService::class.java.name + ".PONG"
    }

    private var mPlayer = MediaPlayer()
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
                if (++mMissedCount > resources.getInteger(R.integer.panic_threshold)) {
                    setState(State.PANIC)
                } else {
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
            if (this.mState == State.PANIC) {
                goPanic()
            }
            updateNotification()
        }
    }

    private fun buildNotification(): Notification {
        var b = Notification.Builder(applicationContext, getString(R.string.channel_id))
        b.setContentTitle("Pinging $mIp")
        b.setContentText("Current state: ${mState.name}")
        b.setSmallIcon(android.R.drawable.ic_dialog_alert)
        b.setChannelId(getString(R.string.channel_id))

        // Main UI pending intent
        val mainUiIntent = Intent(this, MainActivity::class.java)
        mainUiIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        b.setContentIntent(PendingIntent.getActivity(this, 0, mainUiIntent, 0))
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
        mPlayer = MediaPlayer.create(this, R.raw.alarm)
        mPlayer.isLooping = true
        val notification = buildNotification()
        startForeground(resources.getInteger(R.integer.notification_id), notification)
        this.mPingThread.start()
        return START_STICKY
    }

    override fun onDestroy() {
        stopPanic()
        mPlayer.release()
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
        mPlayer.start()
    }

    private fun startHaptic() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val mVibratePattern = longArrayOf(0, 1000, 1000)
            val effect = VibrationEffect.createWaveform(mVibratePattern, 0)
            vibrator.vibrate(effect)
        }
    }

    private fun stopHaptic() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.cancel()
        }
    }

    private fun stopAlarm() {
        if (mPlayer.isPlaying) {
            // Stop and reset media player
            mPlayer.stop()
            mPlayer.release()
            mPlayer = MediaPlayer.create(this, R.raw.alarm)
            mPlayer.isLooping = true
        }
    }

    private fun goPanic() {
        Log.i(gLogTag, "Going panic")
        showUI()
        soundAlarm()
        startHaptic()
    }

    private fun stopPanic() {
        stopAlarm()
        stopHaptic()
    }

    private fun updateNotification() {
        startForeground(resources.getInteger(R.integer.notification_id), buildNotification())
    }
}
