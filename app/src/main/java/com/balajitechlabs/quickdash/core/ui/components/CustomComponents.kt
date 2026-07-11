package com.balajitechlabs.quickdash.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star

enum class SwitchStyle { MaterialYou, Compose, Pixel, Fluent, Cupertino, LiquidGlass }
enum class SliderStyle { Fancy, MaterialYou, Material, HyperOS }
enum class ShapeStyle { Rounded, Cut, Squircle, Smooth }

@Composable
fun StyledSwitch(
    style: SwitchStyle,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    when (style) {
        SwitchStyle.MaterialYou, SwitchStyle.Compose -> {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = modifier
            )
        }
        SwitchStyle.Cupertino -> {
            val thumbOffset by animateDpAsState(if (checked) 20.dp else 2.dp, label = "thumbOffset")
            val trackColor by animateColorAsState(if (checked) Color(0xFF34C759) else Color(0xFFE9E9EA), label = "trackColor")
            Box(
                modifier = modifier
                    .size(51.dp, 31.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(trackColor)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onCheckedChange(!checked) }
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset)
                        .size(27.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
        SwitchStyle.Pixel -> {
            val thumbOffset by animateDpAsState(if (checked) 18.dp else 2.dp, label = "thumbOffset")
            val trackColor by animateColorAsState(if (checked) Color(0xFF1A73E8) else Color(0xFFDADCE0), label = "trackColor")
            val thumbColor by animateColorAsState(if (checked) Color(0xFFFFFFFF) else Color(0xFF757575), label = "thumbColor")
            Box(
                modifier = modifier
                    .size(48.dp, 28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(trackColor)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onCheckedChange(!checked) }
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(thumbColor)
                )
            }
        }
        SwitchStyle.Fluent -> {
            val thumbOffset by animateDpAsState(if (checked) 24.dp else 4.dp, label = "thumbOffset")
            val trackColor by animateColorAsState(if (checked) Color(0xFF0078D4) else Color.Transparent, label = "trackColor")
            val borderColor by animateColorAsState(if (checked) Color(0xFF0078D4) else Color(0xFF8A8886), label = "borderColor")
            val thumbColor by animateColorAsState(if (checked) Color.White else Color(0xFF323130), label = "thumbColor")
            val thumbSize by animateDpAsState(if (checked) 14.dp else 12.dp, label = "thumbSize")
            Box(
                modifier = modifier
                    .size(44.dp, 20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(trackColor)
                    .border(if (checked) 0.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onCheckedChange(!checked) }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = thumbOffset)
                        .size(thumbSize)
                        .clip(CircleShape)
                        .background(thumbColor)
                )
            }
        }
        SwitchStyle.LiquidGlass -> {
            val thumbOffset by animateDpAsState(if (checked) 22.dp else 2.dp, label = "thumbOffset")
            val trackColor by animateColorAsState(if (checked) Color(0x33FF5722) else Color(0x1AFFFFFF), label = "trackColor")
            val borderColor by animateColorAsState(if (checked) Color(0x7FFF5722) else Color(0x33FFFFFF), label = "borderColor")
            val glowColor by animateColorAsState(if (checked) Color(0xFFFF5722) else Color.White, label = "glowColor")
            Box(
                modifier = modifier
                    .size(52.dp, 32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(trackColor)
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onCheckedChange(!checked) }
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset)
                        .size(26.dp)
                        .shadow(if (checked) 6.dp else 2.dp, CircleShape, ambientColor = glowColor, spotColor = glowColor)
                        .clip(CircleShape)
                        .background(glowColor)
                )
            }
        }
    }
}

@Composable
fun StyledSlider(
    style: SliderStyle,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null
) {
    when (style) {
        SliderStyle.MaterialYou, SliderStyle.Material -> {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                onValueChangeFinished = onValueChangeFinished,
                modifier = modifier
            )
        }
        SliderStyle.Fancy -> {
            val progress = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
            val primaryColor = MaterialTheme.colorScheme.primary
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    onValueChangeFinished = onValueChangeFinished,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Transparent,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Canvas(modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .align(Alignment.Center)) {
                    val trackHeight = size.height
                    val width = size.width
                    val cornerRadius = CornerRadius(trackHeight / 2, trackHeight / 2)
                    
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.24f),
                        size = size,
                        cornerRadius = cornerRadius
                    )
                    
                    drawRoundRect(
                        color = primaryColor,
                        size = Size(width * progress, trackHeight),
                        cornerRadius = cornerRadius
                    )
                    
                    drawCircle(
                        color = primaryColor,
                        radius = 8.dp.toPx(),
                        center = Offset(width * progress, trackHeight / 2)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(width * progress, trackHeight / 2)
                    )
                }
            }
        }
        SliderStyle.HyperOS -> {
            val progress = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
            val primaryColor = MaterialTheme.colorScheme.primary
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(primaryColor.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(primaryColor)
                )
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange,
                    onValueChangeFinished = onValueChangeFinished,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Transparent,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.01f)
                )
            }
        }
    }
}

fun starShape(points: Int = 5, rounding: Float = 0.15f): Shape = object : Shape {
    override fun createOutline(size: Size, dir: LayoutDirection, density: Density): Outline {
        val path = Path()
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2
        val r = minOf(w, h) / 2
        val innerR = r * 0.5f
        var angle = -Math.PI / 2
        val angleStep = Math.PI / points
        
        for (i in 0 until 2 * points) {
            val currR = if (i % 2 == 0) r else innerR
            val x = cx + currR * Math.cos(angle)
            val y = cy + currR * Math.sin(angle)
            if (i == 0) {
                path.moveTo(x.toFloat(), y.toFloat())
            } else {
                path.lineTo(x.toFloat(), y.toFloat())
            }
            angle += angleStep
        }
        path.close()
        return Outline.Generic(path)
    }
}

fun cloverShape(points: Int = 4, rounding: Float = 0.4f): Shape = object : Shape {
    override fun createOutline(size: Size, dir: LayoutDirection, density: Density): Outline {
        val path = Path()
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2
        val r = minOf(w, h) / 3
        
        path.addOval(androidx.compose.ui.geometry.Rect(cx - r, cy - 1.5f * r, cx + r, cy + 0.5f * r))
        path.addOval(androidx.compose.ui.geometry.Rect(cx - r, cy - 0.5f * r, cx + r, cy + 1.5f * r))
        path.addOval(androidx.compose.ui.geometry.Rect(cx - 1.5f * r, cy - r, cx + 0.5f * r, cy + r))
        path.addOval(androidx.compose.ui.geometry.Rect(cx - 0.5f * r, cy - r, cx + 1.5f * r, cy + r))
        
        return Outline.Generic(path)
    }
}

fun heartShape(): Shape = object : Shape {
    override fun createOutline(size: Size, dir: LayoutDirection, density: Density): Outline {
        val path = Path()
        val width = size.width
        val height = size.height
        
        path.moveTo(width / 2, height / 5)
        path.cubicTo(5 * width / 6, 0f, width, height / 3, width / 2, height)
        path.cubicTo(0f, height / 3, width / 6, 0f, width / 2, height / 5)
        path.close()
        
        return Outline.Generic(path)
    }
}

fun octagonShape(rounding: Float = 0.15f): Shape = object : Shape {
    override fun createOutline(size: Size, dir: LayoutDirection, density: Density): Outline {
        val path = Path()
        val w = size.width
        val h = size.height
        val offset = minOf(w, h) * rounding
        path.moveTo(offset, 0f)
        path.lineTo(w - offset, 0f)
        path.lineTo(w, offset)
        path.lineTo(w, h - offset)
        path.lineTo(w - offset, h)
        path.lineTo(offset, h)
        path.lineTo(0f, h - offset)
        path.lineTo(0f, offset)
        path.close()
        return Outline.Generic(path)
    }
}

fun pentagonShape(rounding: Float = 0.15f): Shape = object : Shape {
    override fun createOutline(size: Size, dir: LayoutDirection, density: Density): Outline {
        val path = Path()
        val w = size.width
        val h = size.height
        path.moveTo(w / 2, 0f)
        path.lineTo(w, h * 0.38f)
        path.lineTo(w * 0.81f, h)
        path.lineTo(w * 0.19f, h)
        path.lineTo(0f, h * 0.38f)
        path.close()
        return Outline.Generic(path)
    }
}

fun squircleShape(): Shape = object : Shape {
    override fun createOutline(size: Size, dir: LayoutDirection, density: Density): Outline {
        val path = Path()
        val w = size.width
        val h = size.height
        path.moveTo(0f, h / 2)
        path.cubicTo(0f, 0f, 0f, 0f, w / 2, 0f)
        path.cubicTo(w, 0f, w, 0f, w, h / 2)
        path.cubicTo(w, h, w, h, w / 2, h)
        path.cubicTo(0f, h, 0f, h, 0f, h / 2)
        path.close()
        return Outline.Generic(path)
    }
}

fun smoothCornerShape(radiusDp: Float): Shape = object : Shape {
    override fun createOutline(size: Size, dir: LayoutDirection, density: Density): Outline {
        val path = Path()
        val w = size.width
        val h = size.height
        val r = with(density) { radiusDp.dp.toPx() }.coerceAtMost(minOf(w, h) / 2)
        
        path.moveTo(r, 0f)
        path.lineTo(w - r, 0f)
        path.cubicTo(w - r / 2, 0f, w, r / 2, w, r)
        path.lineTo(w, h - r)
        path.cubicTo(w, h - r / 2, w - r / 2, h, w - r, h)
        path.lineTo(r, h)
        path.cubicTo(r / 2, h, 0f, h - r / 2, 0f, h - r)
        path.lineTo(0f, r)
        path.cubicTo(0f, r / 2, r / 2, 0f, r, 0f)
        path.close()
        return Outline.Generic(path)
    }
}

fun getCustomShape(shapeName: String, cornerRadiusDp: Float): Shape {
    return when (shapeName) {
        "Rounded" -> RoundedCornerShape(cornerRadiusDp.dp)
        "Cut" -> CutCornerShape(cornerRadiusDp.dp)
        "Squircle" -> squircleShape()
        "Smooth" -> smoothCornerShape(cornerRadiusDp)
        "Octagon" -> octagonShape()
        "Pentagon" -> pentagonShape()
        "Clover" -> cloverShape()
        "Star" -> starShape()
        "Heart" -> heartShape()
        else -> RoundedCornerShape(cornerRadiusDp.dp)
    }
}