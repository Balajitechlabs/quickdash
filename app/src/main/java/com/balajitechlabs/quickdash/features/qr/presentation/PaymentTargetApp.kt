/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: GNU General Public License v3.0 (GPL-3.0)
 *
 * Feature Module: features/qr/presentation
 * File: PaymentTargetApp.kt
 * Description: Data class and resolver finding installed UPI payment apps on the device.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.qr.presentation

enum class PaymentTargetApp(val displayName: String, val schemePrefix: String) {
    ANY("Any App", "upi://pay"),
    GPAY("Google Pay", "gpay://upi/pay"),
    PHONEPE("PhonePe", "phonepe://upi/pay"),
    PAYTM("Paytm", "paytmmp://pay"),
    BHIM("BHIM", "bhim://pay")
}
