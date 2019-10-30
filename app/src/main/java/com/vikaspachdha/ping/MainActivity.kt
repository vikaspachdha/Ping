package com.vikaspachdha.ping

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var mPlayer = MediaPlayer.create(this, R.raw.alarm)
        //mPlayer.setVolume(1.toFloat(), 1.toFloat())
        //mPlayer.prepare()
        monitorSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mPlayer.start()
            } else if (!isChecked) {
                mPlayer.stop()
                mPlayer = MediaPlayer.create(this, R.raw.alarm)
            }
        }
    }
}
