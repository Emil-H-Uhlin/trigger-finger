package emi.uhl.triggerfinger

import android.graphics.*
import emi.uhl.triggerfinger.game.Game
import emi.uhl.triggerfinger.math.Vector2
import kotlin.math.sin

class LavaBehaviour(private val lavaMinSpeed: Float,
                    private val playerTransform: Transform,
                    private val waveSpeed: Float = 12.5f): Component() {
	
	private val lavaPaint: Paint = Paint().apply {
		color = Color.rgb(1f, (100f / 255f), 0f)
		style = Paint.Style.FILL
	}
	
	private val deepLavaPaint: Paint = Paint().apply {
		color = Color.rgb((209f / 255f), (70f / 255f), 0f)
		style = Paint.Style.FILL
	}
	
	private val speed: Float get() {
		val yDiff = transform.position.y - playerTransform.position.y
		
		if (yDiff < 0)
			return lavaMinSpeed * 3f
		
		return (lavaMinSpeed * Game.toUnits(yDiff)).coerceAtLeast(lavaMinSpeed)
	}
	
	private var xOffset = 0f
	
	override fun update(deltaTime: Float) {
		transform.position += Vector2.up * speed * deltaTime
		updateOffset(deltaTime)
	}
	
	fun updateOffset(deltaTime: Float) {
		xOffset += waveSpeed * deltaTime
	}
	
	override fun draw(canvas: Canvas, paint: Paint?) {
		val path = Path().apply {
			moveTo(transform.position.x, transform.position.y)
			
			var prevY = 0f
			for (x in 0..Game.screenWidth step 25) {
				prevY = transform.position.y + sin(x + xOffset) * 20
				lineTo(x.toFloat(), prevY)
			}
			
			lineTo(Game.screenWidth.toFloat(), prevY)
			lineTo(Game.screenWidth.toFloat(), Game.screenHeight.toFloat())
			lineTo(transform.position.x, Game.screenHeight.toFloat())
		}
		
		canvas.drawPath(path, lavaPaint)
		
		canvas.drawPath(path.apply { 
			transform(Matrix().apply {
				postTranslate(-Game.screenWidth.toFloat(), 125f)
				postScale(-1f, 1f) })}, deepLavaPaint)
	}
}