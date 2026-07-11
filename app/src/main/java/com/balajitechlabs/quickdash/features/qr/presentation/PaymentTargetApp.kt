package com.balajitechlabs.quickdash.features.qr.presentation

enum class PaymentTargetApp(val displayName: String, val schemePrefix: String) {
    ANY("Any App", "upi://pay"),
    GPAY("Google Pay", "gpay://upi/pay"),
    PHONEPE("PhonePe", "phonepe://upi/pay"),
    PAYTM("Paytm", "paytmmp://pay"),
    BHIM("BHIM", "bhim://pay")
}
