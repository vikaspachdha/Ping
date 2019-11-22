package com.vikaspachdha.ping

import android.content.Context
import android.util.Log
import android.widget.TextView
import androidx.collection.CircularArray
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


object PingLogger {
    private const val gLogTag: String = "PingLogger"
    private const val mFileName: String = "log.txt"

    private var mLogs = CircularArray<String>()
    private var mLogLimit = 280
    private var mInitialized = false

    var LogView: TextView? = null
    set(value) {
        field = value
        refreshView()
    }

    var LogSavePath: String = ""
    set(value) {
        if (LogSavePath != value) {
            field = value
            loadLogs()
        }
    }



    fun addLog(msg: String, withTimeStamp: Boolean = true) {
        val logMsg = if (withTimeStamp) "${timeStamp()} $msg" else msg
        if (mLogs.size() == mLogLimit) {
            mLogs.popLast()
        }
        mLogs.addFirst(logMsg)
        prependLogToView(logMsg)
    }


    fun loadLogs() {
        try {
            val f = File(LogSavePath, mFileName)
            if (!f.exists()) {
                Log.d(gLogTag, "Reading logs. Log file $mFileName does not exists")
                return
            }
            LogView?.text = ""
            f.forEachLine { line ->
                addLog(line, false)
            }
        } catch (e: Exception) {
            Log.d(gLogTag, "Reading logs failed. $e")
        }
    }


    fun saveLogs() {
        try {
            val f = File(LogSavePath, mFileName)
            if (f.exists())
                f.delete()
            f.createNewFile()
            f.setWritable(true)
            var logs = ""
            for (index in 0 until mLogs.size()) {
                logs = "${mLogs.get(index)}\n$logs"
            }
            f.writeText(logs)
        } catch (e: Exception) {
            Log.d(gLogTag, "Writing logs failed. $e")
        }
    }

    fun clear() {
        LogView?.text = ""
        mLogs.clear()
        val f = File(LogSavePath, mFileName)
        if (f.exists())
            f.delete()
    }

    private fun timeStamp(): String {
        return SimpleDateFormat("dd--HH:mm:ss:SSS").format(Date())
    }

    private fun prependLogToView(msg: String) {
        LogView?.text = "$msg\n${LogView?.text}"
    }

    private fun refreshView() {
        LogView?.text = ""
        for (index in 0 until mLogs.size()) {
            prependLogToView(mLogs.get(index))
        }
    }
}