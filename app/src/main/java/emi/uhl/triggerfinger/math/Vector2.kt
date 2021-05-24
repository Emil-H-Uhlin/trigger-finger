package emi.uhl.triggerfinger.math

import kotlin.math.*

/**
 * Two dimensional vector for vector movement and position logic of game objects
 * @author Emil Uhlin, EMUH0001
 */
data class Vector2(var x: Float, var y: Float) {
    companion object {
        val zero : Vector2 =
            Vector2(.0f, .0f); get() = field.copy()
        val right : Vector2 =
            Vector2(1.0f, .0f); get() = field.copy()
        val left : Vector2 =
            Vector2(-1.0f, .0f); get() = field.copy()
        val up : Vector2 =
            Vector2(.0f, -1.0f); get() = field.copy()
        val down : Vector2 =
            Vector2(.0f, 1.0f); get() = field.copy()

        fun dot(vector: Vector2, other: Vector2) : Float {
            val ang = atan2(other.y, other.x) - atan2(vector.y, vector.x)
            return vector.length * other.length * cos(ang)
        }

        fun distance(vector: Vector2, other: Vector2) : Float {
            return (other - vector).length
        }

        fun angle(vector: Vector2) : Float {
            return atan2(vector.y, vector.x)
        }

        fun angleBetween(vector: Vector2, other: Vector2) : Float {
            val delta = other - vector

            return atan2(delta.y, delta.x)
        }
    }

    constructor(x: Int, y: Int): this(x.toFloat(), y.toFloat())
    constructor(x: Float, y: Int): this(x, y.toFloat())
    constructor(x: Int, y: Float): this(x.toFloat(), y)
    
    operator fun plusAssign(other: Vector2) { x += other.x; y += other.y }  // add vector
    operator fun minusAssign(other: Vector2) { x -= other.x; y -= other.y } // subtract vector

    operator fun times(value: Float) = Vector2(x * value, y * value)    // scaled vector
    operator fun timesAssign(value: Float) { x *= value; y *= value } // scale vector

    operator fun div(value: Float) = Vector2(x / value, y / value) // scaled vector
    operator fun divAssign(value: Float) { x /= value; y /= value } // scale vector

    operator fun unaryMinus() = Vector2(-x, -y) // opposite vector
    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)

    val length : Float get() = sqrt(lengthSquared)

    val lengthSquared : Float get() = x * x + y * y

    inline val normalized : Vector2
        get() {
            val v = copy()
            v.normalize()

            return v
        }

    fun normalize() {
        if (length == .0f)
            return

        this /= length
    }

    override fun toString(): String = "($x, $y)"
}