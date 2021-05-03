package emi.uhl.triggerfinger.physics

import emi.uhl.triggerfinger.math.Vector2

object Physics {
	const val PLAYER: Int = 1 shl 0
	const val ENEMY: Int = 1 shl 1
	const val ENVIRONMENT: Int = 1 shl 2
	
	const val DAMPING: Float = 0.01f
	val GRAVITY: Vector2 = Vector2.down * 750f; get() = field.copy()
}