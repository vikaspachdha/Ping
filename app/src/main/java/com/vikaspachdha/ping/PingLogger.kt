package com.vikaspachdha.ping

import android.content.Context
import android.util.Log
import android.widget.TextView
import androidx.collection.CircularArray
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class PingLogger {
    val gLogTag = "PingLogger"
    private var mLogs = CircularArray<String>()
    private var mLogLimit = 32
    private var mFilePath: String = ""
    private var mFileName: String = ""
    private var mLogView: TextView? = null

    constructor(context: Context, limit: Int, fileName: String, view: TextView) {
        if (limit > 0)
            mLogLimit = limit

        mFilePath = context.filesDir.absolutePath
        mFileName = if (fileName.isEmpty()) "log.txt" else fileName
        mLogView = view
    }

    fun addLog(msg: String, withTimeStamp: Boolean = true) {
        val timeStamp = SimpleDateFormat("dd--HH:mm:ss:SSS").format(Date())
        val logMsg = if (withTimeStamp) "$timeStamp $msg" else msg
        if (mLogs.size() == mLogLimit) {
            mLogs.popLast()
        }
        mLogs.addFirst(logMsg)
        prependLogToView(logMsg)
    }

    fun prependLogToView(msg: String) {
        mLogView?.text = "$msg\n${mLogView?.text}"
    }

    fun loadLogs() {
        try {
            val f = File(mFilePath, mFileName)
            if (!f.exists()) {
                Log.d(gLogTag, "Reading logs. Log file $mFileName does not exists")
                return
            }
            mLogView?.text = ""
            f.forEachLine { line ->
                addLog(line, false)
            }
        } catch (e: Exception) {
            Log.d(gLogTag, "Reading logs failed. $e")
        }
    }

    fun saveLogs() {
        try {
            val f = File(mFilePath, mFileName)
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
        mLogView?.text = ""
        mLogs.clear()
        val f = File(mFilePath, mFileName)
        if (f.exists())
            f.delete()
    }
}