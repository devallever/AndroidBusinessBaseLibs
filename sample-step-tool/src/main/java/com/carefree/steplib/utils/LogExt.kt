package com.carefree.steplib.utils

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.carefree.steplib.utils.StepTracker

/**
 *classDes:LogExt
 *author: CHL
 *create date: 2022/10/20
 */

fun logD(msg: String?) {
    logD("TestLog", msg)
}

fun logE(msg: String?) {
    logE("TestLog", msg)
}

fun Fragment.logD(msg: String) {
    logD(javaClass.simpleName, msg)
}

fun Activity.logD(msg: String) {
    logD(javaClass.simpleName, msg)
}


fun View.logD(msg: String) {
    logD(javaClass.simpleName, msg)
}


fun Fragment.logE(msg: String) {
    logE(javaClass.simpleName, msg)
}

fun Activity.logE(msg: String) {
    logE(javaClass.simpleName, msg)
}

fun View.logE(msg: String) {
    logE(javaClass.simpleName, msg)
}

fun logD(tag: String, msg: String?) {
    if (StepTracker.isDebugMode) {
        Log.d(tag, logLine() +(msg ?: ""))
    }
}

fun logE(tag: String, msg: String?) {
    if (StepTracker.isDebugMode) {
        Log.e(tag, logLine() +(msg ?: ""))
    }
}

private fun logLine():String {
    val trace = Thread.currentThread().stackTrace
    val dataList: MutableList<String> = ArrayList()
    for (i in trace.indices.reversed()) {
        val e = trace[i]
        val className = e.className //带包名
        val fileName = e.fileName //带包名
        if (e.fileName != null && !fileName.contains("LogExt") && className.startsWith(StepTracker.packageName)) {
            val indexOf = e.methodName.indexOf("\$lambda")
            val data = "(${e.fileName}:${e.lineNumber}).${if (indexOf!=-1) e.methodName.substring(0,indexOf) else e.methodName}()"
            dataList.add(data)
        }
    }
    return if (dataList.size > 0) {
        "${dataList[dataList.size - 1]} log ---> "
    }else {
        ""
    }
}
