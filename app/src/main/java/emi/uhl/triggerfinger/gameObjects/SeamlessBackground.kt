package emi.uhl.triggerfinger.gameObjects

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import emi.uhl.triggerfinger.game.Game
import kotlin.math.roundToInt

class SeamlessBackground(srcBitmap: Bitmap,
                         private val backgroundSpeed: Float): Component() {
	
	private val backgroundImage: Bitmap = let {
		val scale = (Game.screenHeight.toFloat() / srcBitmap.height.toFloat())
		val width = srcBitmap.width * scale
		val height = srcBitmap.height * scale
		
		Bitmap.createScaledBitmap(srcBitmap, width.toInt(), height.toInt(), false)
	}
	
	private var backgroundX = 0f
	
	override fun update(deltaTime: Float) {
		backgroundX -= deltaTime * backgroundSpeed
		
		if (backgroundX < -backgroundImage.width) {
			backgroundX = 0f
		}
	}
	
	override fun draw(canvas: Canvas, paint: Paint?) {
		for (x in backgroundX.roundToInt() until Game.screenWidth step backgroundImage.width) {
			canvas.drawBitmap(backgroundImage, x.toFloat(), 0f, paint)
		}
	}
}