package emi.uhl.triggerfinger

import android.graphics.*
import emi.uhl.triggerfinger.math.MathHelper
import emi.uhl.triggerfinger.math.Vector2
import kotlin.math.sin

class LavaBehaviour(private val lavaMinSpeed: Float,
                    private val playerTransform: Transform,
                    private val waveSpeed: Float = 12.5f): Component() {
	
	private val lavaPaint: Paint = Paint().apply {
		color = Color.rgb(1f, (100f / 255f), 0f)
		style = Paint.Style.FILL
	}
	
	private val speed: Float get() {
		val yDiff = transform.position.y - playerTransform.position.y
		
		return (lavaMinSpeed * yDiff / 500f).coerceAtLeast(lavaMinSpeed)
	}
	
	private var xOffset = 0f
	
	override fun update(deltaTime: Float) {
		transform.position += Vector2.up * speed * deltaTime
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
	}
}