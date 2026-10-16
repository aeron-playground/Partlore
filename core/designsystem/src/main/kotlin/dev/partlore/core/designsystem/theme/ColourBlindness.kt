package dev.partlore.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces

/** How pin colours look with full red-green colour blindness (Machado, Oliveira and Fernandes 2009). */
internal enum class ColourBlindness(private val m: FloatArray) {
    Deuteranopia(
        floatArrayOf(
            0.367322f, 0.860646f, -0.227968f,
            0.280085f, 0.672501f, 0.047413f,
            -0.011820f, 0.042940f, 0.968881f,
        ),
    ),
    Protanopia(
        floatArrayOf(
            0.152286f, 1.052583f, -0.204868f,
            0.114503f, 0.786281f, 0.099216f,
            -0.003882f, -0.048116f, 1.051998f,
        ),
    ),
    ;

    // The matrices work on linear light, so the colour leaves sRGB first and returns after.
    fun simulate(color: Color): Color {
        val c = color.convert(ColorSpaces.LinearSrgb)
        fun row(i: Int) = (m[i] * c.red + m[i + 1] * c.green + m[i + 2] * c.blue).coerceIn(0f, 1f)
        return Color(row(0), row(3), row(6), c.alpha, ColorSpaces.LinearSrgb).convert(ColorSpaces.Srgb)
    }

    fun simulate(pins: PinColors): PinColors {
        fun PinColor.seen() = PinColor(simulate(fill), simulate(ink))
        return PinColors(
            power = pins.power.seen(),
            ground = pins.ground.seen(),
            i2c = pins.i2c.seen(),
            spi = pins.spi.seen(),
            uart = pins.uart.seen(),
            adc = pins.adc.seen(),
            pwm = pins.pwm.seen(),
            touch = pins.touch.seen(),
            strapping = pins.strapping.seen(),
            doNotUse = pins.doNotUse.seen(),
            gpio = pins.gpio.seen(),
        )
    }
}
