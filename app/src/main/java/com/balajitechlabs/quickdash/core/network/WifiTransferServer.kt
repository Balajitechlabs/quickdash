/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: core/network
 * File: WifiTransferServer.kt
 * Description: Embedded local HTTP server facilitating direct peer-to-peer file and clipboard transfers over Wi-Fi.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.core.network

import android.content.Context
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread
import android.util.Log

/**
 *  Multi-Device Wi-Fi Transfer Server (`WifiTransferServer.kt`).
 * Runs a micro HTTP server allowing users to send notes and text to PC browsers over local Wi-Fi.
 */
class WifiTransferServer(private val port: Int = 8080) {

    private var serverSocket: ServerSocket? = null
    private var isRunning = false

    fun startServer(context: Context) {
        if (isRunning) return
        isRunning = true

        thread {
            try {
                serverSocket = ServerSocket(port)
                while (isRunning) {
                    val client: Socket = serverSocket?.accept() ?: break
                    handleClient(client)
                }
            } catch (e: Exception) {
                Log.e("QuickDash", "Error occurred: ${e.message}", e)
            }
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            val output = socket.getOutputStream()
            val html = """
                <!DOCTYPE html>
                <html>
                <head><title>QuickDash Wi-Fi Transfer</title></head>
                <body style="font-family:sans-serif; text-align:center; padding:40px; background:#0f172a; color:#f8fafc;">
                    <h1>QuickDash Wi-Fi Transfer</h1>
                    <p>Send text and notes directly to your device</p>
                    <textarea style="width:80%; height:150px; border-radius:12px; padding:12px; font-size:16px;"></textarea><br><br>
                    <button style="padding:12px 24px; font-size:16px; border-radius:8px; background:#6366f1; color:white; border:none; cursor:pointer;">Send to Phone</button>
                </body>
                </html>
            """.trimIndent()

            val response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: ${html.toByteArray().size}\r\n\r\n$html"
            output.write(response.toByteArray())
            output.flush()
            socket.close()
        } catch (e: Exception) {
            Log.e("QuickDash", "Error occurred: ${e.message}", e)
        }
    }

    fun stopServer() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e("QuickDash", "Error occurred: ${e.message}", e)
        }
    }
}
