package emi.uhl.triggerfinger.game

object Game {
	const val PIXELS_IN_UNIT: Int = 200
	
	@JvmStatic var screenWidth: Int = -1
	@JvmStatic var screenHeight: Int = -1
	
	@JvmStatic var timeScale: Float = 1.0f
	
	@JvmStatic fun toUnits(value: Float): Float = value / PIXELS_IN_UNIT
	@JvmStatic fun toPixels(value: Float): Float = value * PIXELS_IN_UNIT
}