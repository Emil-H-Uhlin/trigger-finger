package emi.uhl.triggerfinger.game

object Game {
	const val pixelsInUnit: Int = 250
	
	@JvmStatic var screenWidth: Int = -1
	@JvmStatic var screenHeight: Int = -1
	
	@JvmStatic var timeScale: Float = 1.0f
	
	@JvmStatic fun toUnits(value: Float): Float = value / pixelsInUnit
}