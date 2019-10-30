package com.vikaspachdha.ping

import android.media.RingtoneManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val r =  RingtoneManager.getRingtone(this, uri)
        monitorSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && !r.isPlaying) {
                r.play();
            } else if (!isChecked && r.isPlaying) {
                r.stop();
            }
        }
    }
}
