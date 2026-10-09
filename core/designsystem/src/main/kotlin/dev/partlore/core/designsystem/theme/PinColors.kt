package dev.partlore.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/** What a pin's dot shows: its main function, or that it must not be used. */
enum class PinKind { Power, Ground, I2c, Spi, Uart, Adc, Pwm, Touch, DoNotUse, Gpio }

/** A pin dot's fill and the colour of the glyph on it (≥ 4.5:1, checked in ContrastTest). */
data class PinColor(val fill: Color, val ink: Color)

data class PinColors(
    val power: PinColor,
    val ground: PinColor,
    val i2c: PinColor,
    val spi: PinColor,
    val uart: PinColor,
    val adc: PinColor,
    val pwm: PinColor,
    val touch: PinColor,
    val strapping: PinColor,
    val doNotUse: PinColor,
    val gpio: PinColor,
) {
    fun of(kind: PinKind): PinColor = when (kind) {
        PinKind.Power -> power
        PinKind.Ground -> ground
        PinKind.I2c -> i2c
        PinKind.Spi -> spi
        PinKind.Uart -> uart
        PinKind.Adc -> adc
        PinKind.Pwm -> pwm
        PinKind.Touch -> touch
        PinKind.DoNotUse -> doNotUse
        PinKind.Gpio -> gpio
    }
}

private val Black = Color(0xFF000000)
private val White = Color(0xFFFFFFFF)

// Okabe–Ito based: safe for the common types of colour blindness. Glyphs carry the meaning too.
private fun okabeIto(ground: PinColor, gpio: PinColor) = PinColors(
    power = PinColor(Color(0xFFD55E00), Black),
    ground = ground,
    i2c = PinColor(Color(0xFF0072B2), White),
    spi = PinColor(Color(0xFFCC79A7), Black),
    uart = PinColor(Color(0xFF009E73), Black),
    adc = PinColor(Color(0xFFE69F00), Black),
    pwm = PinColor(Color(0xFF56B4E9), Black),
    touch = PinColor(Color(0xFF56B4E9), Black),
    strapping = PinColor(Color(0xFFF0E442), Black),
    doNotUse = PinColor(Color(0xFF8E8A99), Black),
    gpio = gpio,
)

internal val LightPins = okabeIto(PinColor(Color(0xFF4A4A4A), White), PinColor(Color(0xFFDDD5C8), Color(0xFF1C1B22)))
internal val DarkPins = okabeIto(PinColor(Color(0xFF9A9A9A), Black), PinColor(Color(0xFF3A3548), Color(0xFFF2EFF7)))

private val RedPin = PinColor(Color(0xFFFF6B5E), Color(0xFF0B0A0F))

internal val RedLightPins =
    PinColors(
        power = RedPin,
        ground = RedPin,
        i2c = RedPin,
        spi = RedPin,
        uart = RedPin,
        adc = RedPin,
        pwm = RedPin,
        touch = RedPin,
        strapping = RedPin,
        doNotUse = PinColor(Color(0xFF3A1210), Color(0xFFFF6B5E)),
        gpio = PinColor(Color(0xFF2A0F0C), Color(0xFFFF8A80)),
    )
