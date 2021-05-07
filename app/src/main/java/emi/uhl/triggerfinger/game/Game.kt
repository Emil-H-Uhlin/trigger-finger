package emi.uhl.triggerfinger.game

object Game {
	const val pixelsInUnit: Int = 250
	
	var screenWidth: Int = -1
	var screenHeight: Int = -1
	
	var timeScale: Float = 1.0f
	
	fun toUnits(value: Float): Float = value / pixelsInUnit
}