package emi.uhl.triggerfinger.math

/**
 * @author Emil Uhlin, EMUH0001
 * Math-helper functions that may be useful in some scenarios
 */
object MathHelper {
    fun fraction(value: Float, begin: Float, end: Float) : Float {
        return (value - begin) / (end - begin)
    }

    fun fractal(fraction: Float, begin: Float, end: Float) : Float {
        return fraction * (end - begin) + begin
    }
}