package com.balajitechlabs.quickdash.features.qr.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * 📷 Safe QR Scanner Helper (`QrScannerHelper.kt`)
 * Invokes Google Code Scanner via pure runtime reflection if present,
 * or gracefully falls back to system scanner / ZXing with ZERO compile-time Google ML Kit dependencies.
 */
object QrScannerHelper {

    fun startScan(
        context: Context,
        onResult: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        try {
            val scannerClass = Class.forName("com.google.mlkit.vision.codescanner.GmsBarcodeScanning")
            val getClientMethod = scannerClass.getMethod("getClient", Context::class.java)
            val scannerInstance = getClientMethod.invoke(null, context)
            val startScanMethod = scannerInstance.javaClass.getMethod("startScan")
            val taskInstance = startScanMethod.invoke(scannerInstance) ?: run {
                onError("Scanner task initialization failed")
                return
            }

            // Reflectively attach addOnSuccessListener
            val successListenerClass = Class.forName("com.google.android.gms.tasks.OnSuccessListener")
            val proxyHandler = java.lang.reflect.InvocationHandler { _, method, args ->
                if (method.name == "onSuccess" && args != null && args.isNotEmpty()) {
                    try {
                        val barcode = args[0]
                        val getRawValueMethod = barcode.javaClass.getMethod("getRawValue")
                        val rawValue = getRawValueMethod.invoke(barcode) as? String
                        if (!rawValue.isNullOrBlank()) {
                            onResult(rawValue)
                        } else {
                            onError("No barcode data found")
                        }
                    } catch (e: Exception) {
                        onError("Failed to read barcode: ${e.message}")
                    }
                }
                null
            }

            val proxyListener = java.lang.reflect.Proxy.newProxyInstance(
                successListenerClass.classLoader,
                arrayOf(successListenerClass),
                proxyHandler
            )

            val addSuccessMethod = taskInstance.javaClass.getMethod("addOnSuccessListener", successListenerClass)
            addSuccessMethod.invoke(taskInstance, proxyListener)

            // Reflectively attach addOnFailureListener
            val failureListenerClass = Class.forName("com.google.android.gms.tasks.OnFailureListener")
            val failureProxyHandler = java.lang.reflect.InvocationHandler { _, method, args ->
                if (method.name == "onFailure" && args != null && args.isNotEmpty()) {
                    val ex = args[0] as? Exception
                    onError("Scanner failed: ${ex?.localizedMessage ?: ex?.message ?: "Unknown error"}")
                }
                null
            }

            val failureProxyListener = java.lang.reflect.Proxy.newProxyInstance(
                failureListenerClass.classLoader,
                arrayOf(failureListenerClass),
                failureProxyHandler
            )

            val addFailureMethod = taskInstance.javaClass.getMethod("addOnFailureListener", failureListenerClass)
            addFailureMethod.invoke(taskInstance, failureProxyListener)

        } catch (_: Throwable) {
            // FOSS Mode: Launch system scanner intent
            try {
                val intent = Intent("com.google.zxing.client.android.SCAN").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Please install a QR Scanner app or ZXing Barcode Scanner.", Toast.LENGTH_LONG).show()
                onError("Scanner component not available on this device")
            }
        }
    }
}
