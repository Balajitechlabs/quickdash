/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/shizuku
 * File: ShizukuHelper.kt
 * Description: EssentialX-styled component for core/shizuku supporting high performance productivity tools.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

/**
 * Lightweight Shizuku helper to check availability/permission and run shell commands.
 * Used only where Shizuku unlocks capabilities unavailable through public APIs.
 */
object ShizukuHelper {

    private var binderReceived = false

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        binderReceived = true
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        binderReceived = false
    }

    private val permissionListeners = java.util.concurrent.CopyOnWriteArrayList<(Boolean) -> Unit>()

    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        val granted = grantResult == PackageManager.PERMISSION_GRANTED
        permissionListeners.forEach { it(granted) }
    }

    init {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
        } catch (_: Throwable) {}
    }

    val isAvailable: Boolean
        get() = try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }

    val isPermissionGranted: Boolean
        get() = try {
            if (!isAvailable) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (_: Throwable) {
            false
        }

    fun addPermissionListener(listener: (Boolean) -> Unit) {
        permissionListeners.add(listener)
    }

    fun removePermissionListener(listener: (Boolean) -> Unit) {
        permissionListeners.remove(listener)
    }

    fun requestPermission(requestCode: Int = 9001) {
        try {
            if (isAvailable && !isPermissionGranted) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (_: Throwable) {}
    }

    /**
     * Run a privileged shell command via Shizuku and return trimmed stdout.
     * Returns null on any failure.
     */
    fun runCommand(command: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val out = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            out.ifEmpty { null }
        } catch (_: Exception) { null }
    }
}
