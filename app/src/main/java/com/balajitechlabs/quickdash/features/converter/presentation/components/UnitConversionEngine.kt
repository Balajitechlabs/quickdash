/*
 * Copyright (c) 2026 ||BTL||™ (balajitechlabs)
 * License: PocketOps Custom Open Source Fork License
 *
 * Feature Module: features/converter/presentation/components
 * File: UnitConversionEngine.kt
 * Description: Mathematical conversion algorithms and unit definitions across scientific categories.
 * Developer: balajitechlabs
 */
package com.balajitechlabs.quickdash.features.converter.presentation.components

enum class UnitCategory(val displayName: String, val units: List<String>) {
    LENGTH(
        "Length",
        listOf("Meters (m)", "Kilometers (km)", "Feet (ft)", "Inches (in)", "Miles (mi)", "Centimeters (cm)", "Millimeters (mm)")
    ),
    WEIGHT(
        "Weight",
        listOf("Kilograms (kg)", "Grams (g)", "Pounds (lbs)", "Ounces (oz)", "Milligrams (mg)", "Tonnes (t)")
    ),
    TEMPERATURE(
        "Temperature",
        listOf("Celsius (°C)", "Fahrenheit (°F)", "Kelvin (K)")
    ),
    AREA(
        "Area",
        listOf("Sq Meters (m²)", "Sq Feet (ft²)", "Acres", "Hectares", "Sq Kilometers (km²)")
    ),
    VOLUME(
        "Volume",
        listOf("Liters (L)", "Milliliters (mL)", "Gallons (US)", "Cups", "Pints", "Fluid Oz")
    ),
    SPEED(
        "Speed",
        listOf("m/s", "km/h", "mph", "knots", "ft/s")
    ),
    DATA(
        "Data",
        listOf("Bytes (B)", "Kilobytes (KB)", "Megabytes (MB)", "Gigabytes (GB)", "Terabytes (TB)")
    )
}

fun convertUnits(category: UnitCategory, fromIdx: Int, toIdx: Int, value: Double): Double {
    if (fromIdx == toIdx) return value
    return when (category) {
        UnitCategory.LENGTH -> {
            val meters = when (fromIdx) {
                0 -> value
                1 -> value * 1000.0
                2 -> value * 0.3048
                3 -> value * 0.0254
                4 -> value * 1609.344
                5 -> value * 0.01
                6 -> value * 0.001
                else -> value
            }
            when (toIdx) {
                0 -> meters
                1 -> meters / 1000.0
                2 -> meters / 0.3048
                3 -> meters / 0.0254
                4 -> meters / 1609.344
                5 -> meters / 0.01
                6 -> meters / 0.001
                else -> meters
            }
        }
        UnitCategory.WEIGHT -> {
            val kg = when (fromIdx) {
                0 -> value
                1 -> value / 1000.0
                2 -> value * 0.453592
                3 -> value * 0.0283495
                4 -> value / 1_000_000.0
                5 -> value * 1000.0
                else -> value
            }
            when (toIdx) {
                0 -> kg
                1 -> kg * 1000.0
                2 -> kg / 0.453592
                3 -> kg / 0.0283495
                4 -> kg * 1_000_000.0
                5 -> kg / 1000.0
                else -> kg
            }
        }
        UnitCategory.TEMPERATURE -> {
            val celsius = when (fromIdx) {
                0 -> value
                1 -> (value - 32.0) * (5.0 / 9.0)
                2 -> value - 273.15
                else -> value
            }
            when (toIdx) {
                0 -> celsius
                1 -> celsius * 9.0 / 5.0 + 32.0
                2 -> celsius + 273.15
                else -> celsius
            }
        }
        UnitCategory.AREA -> {
            val sqM = when (fromIdx) {
                0 -> value
                1 -> value * 0.092903
                2 -> value * 4046.856
                3 -> value * 10000.0
                4 -> value * 1_000_000.0
                else -> value
            }
            when (toIdx) {
                0 -> sqM
                1 -> sqM / 0.092903
                2 -> sqM / 4046.856
                3 -> sqM / 10000.0
                4 -> sqM / 1_000_000.0
                else -> sqM
            }
        }
        UnitCategory.VOLUME -> {
            val liters = when (fromIdx) {
                0 -> value
                1 -> value / 1000.0
                2 -> value * 3.78541
                3 -> value * 0.236588
                4 -> value * 0.473176
                5 -> value * 0.0295735
                else -> value
            }
            when (toIdx) {
                0 -> liters
                1 -> liters * 1000.0
                2 -> liters / 3.78541
                3 -> liters / 0.236588
                4 -> liters / 0.473176
                5 -> liters / 0.0295735
                else -> liters
            }
        }
        UnitCategory.SPEED -> {
            val ms = when (fromIdx) {
                0 -> value
                1 -> value / 3.6
                2 -> value * 0.44704
                3 -> value * 0.514444
                4 -> value * 0.3048
                else -> value
            }
            when (toIdx) {
                0 -> ms
                1 -> ms * 3.6
                2 -> ms / 0.44704
                3 -> ms / 0.514444
                4 -> ms / 0.3048
                else -> ms
            }
        }
        UnitCategory.DATA -> {
            val bytes = when (fromIdx) {
                0 -> value
                1 -> value * 1024.0
                2 -> value * 1024.0 * 1024.0
                3 -> value * 1024.0.pow(3)
                4 -> value * 1024.0.pow(4)
                else -> value
            }
            when (toIdx) {
                0 -> bytes
                1 -> bytes / 1024.0
                2 -> bytes / (1024.0 * 1024.0)
                3 -> bytes / 1024.0.pow(3)
                4 -> bytes / 1024.0.pow(4)
                else -> bytes
            }
        }
    }
}

private fun Double.pow(n: Int): Double {
    var result = 1.0
    repeat(n) { result *= this }
    return result
}
