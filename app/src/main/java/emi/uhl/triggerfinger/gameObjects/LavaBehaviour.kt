package emi.uhl.triggerfinger.gameObjects

import android.graphics.*
import emi.uhl.triggerfinger.game.GameRules
import emi.uhl.triggerfinger.math.Vector2
import kotlin.math.sin

class LavaBehaviour(private val lavaMinSpeed: Float,
                    private val playerTransform: Transform,
                    private val waveSpeed: Float = 12.5f): Component() {
	
	/**
	 * Color for highest level of lava
	 */
	private val lavaPaint: Paint = Paint().apply {
		color = Color.rgb(1f, (100f / 255f), 0f)
		style = Paint.Style.FILL
	}
	
	/**
	 * Color for deeper lava
	 */
	private val deepLavaPaint: Paint = Paint().apply {
		color = Color.rgb((209f / 255f), (70f / 255f), 0f)
		style = Paint.Style.FILL
	}
	
	/**
	 * Speed of lava
	 * Faster lava when player is far away
	 */
	private val speed: Float get() {
		val yDiff = transform.position.y - playerTransform.position.y
		
		if (yDiff < 0)
			return lavaMinSpeed * 3f
		
		return (lavaMinSpeed * GameRules.toUnits(yDiff)).coerceAtLeast(lavaMinSpeed)
	}
	
	private var xOffset = 0f // used for path generation
	
	override fun update(deltaTime: Float) {
		transform.position += Vector2.up * speed * deltaTime
		updateOffset(deltaTime)
	}
	
	fun updateOffset(deltaTime: Float) {
		xOffset += waveSpeed * deltaTime
	}
	
	/**
	 * Generates a path that gets filled to render lava
	 * Uses a matrix to transform (flip by scale -1 and translate downwards)
	 */
	override fun draw(canvas: Canvas, paint: Paint?) {
		val path = Path().apply {
			moveTo(transform.position.x, transform.position.y)
			
			var prevY = 0f
			for (x in 0..GameRules.screenWidth step 25) {
				prevY = transform.position.y + sin(x + xOffset) * 20
				lineTo(x.toFloat(), prevY)
			}
			
			lineTo(GameRules.screenWidth.toFloat(), prevY)
			lineTo(GameRules.screenWidth.toFloat(), GameRules.screenHeight.toFloat())
			lineTo(transform.position.x, GameRules.screenHeight.toFloat())
		}
		
		canvas.drawPath(path, lavaPaint)
		
		canvas.drawPath(path.apply { 
			transform(Matrix().apply {
				postTranslate(-GameRules.screenWidth.toFloat(), 125f)
				postScale(-1f, 1f) })}, deepLavaPaint)
	}
}