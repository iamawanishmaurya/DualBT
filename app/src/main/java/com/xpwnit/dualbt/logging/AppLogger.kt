package com.xpwnit.dualbt.logging

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque

enum class LogLevel(val label: String, val priority: Int) {
    DEBUG("DEBUG", Log.DEBUG),
    INFO("INFO", Log.INFO),
    WARNING("WARN", Log.WARN),
    ERROR("ERROR", Log.ERROR)
}

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null
) {
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun formatted(): String {
        val time = dateFormat.format(Date(timestamp))
        val err = throwable?.let { " | ${it.message}" } ?: ""
        return "[$time] [${level.label}] [$tag] $message$err"
    }
}

object AppLogger {
    private const val MAX_LOG_ENTRIES = 500
    private const val APP_TAG = "DualBT"

    private val _logs = ConcurrentLinkedDeque<LogEntry>()
    private val _logsFlow = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logsFlow

    var minLevel: LogLevel = LogLevel.DEBUG

    fun d(tag: String, message: String) = log(LogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(LogLevel.INFO, tag, message)
    fun w(tag: String, message: String) = log(LogLevel.WARNING, tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) =
        log(LogLevel.ERROR, tag, message, throwable)

    private fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        if (level.priority < minLevel.priority) return

        // Android Logcat
        val fullTag = "$APP_TAG:$tag"
        when (level) {
            LogLevel.DEBUG -> Log.d(fullTag, message)
            LogLevel.INFO -> Log.i(fullTag, message)
            LogLevel.WARNING -> Log.w(fullTag, message)
            LogLevel.ERROR -> {
                if (throwable != null) Log.e(fullTag, message, throwable)
                else Log.e(fullTag, message)
            }
        }

        // In-memory log store
        val entry = LogEntry(level = level, tag = tag, message = message, throwable = throwable)
        _logs.addFirst(entry)
        while (_logs.size > MAX_LOG_ENTRIES) {
            _logs.removeLast()
        }
        _logsFlow.value = _logs.toList()
    }

    fun clear() {
        _logs.clear()
        _logsFlow.value = emptyList()
    }

    fun getFilteredLogs(level: LogLevel? = null, tag: String? = null): List<LogEntry> {
        return _logs.filter { entry ->
            (level == null || entry.level == level) &&
            (tag == null || entry.tag.contains(tag, ignoreCase = true))
        }
    }
}
