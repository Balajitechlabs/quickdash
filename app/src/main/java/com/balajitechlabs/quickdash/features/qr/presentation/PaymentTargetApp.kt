/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/qr
 * File: PaymentTargetApp.kt
 * Description: EssentialX-styled component for features/qr supporting high performance productivity tools.
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
