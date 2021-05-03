package emi.uhl.triggerfinger.math

object MathHelper {
    fun fraction(value: Float, begin: Float, end: Float) : Float {
        return (value - begin) / (end - begin)
    }

    fun fractal(fraction: Float, begin: Float, end: Float) : Float {
        return fraction * (end - begin) + begin
    }
}