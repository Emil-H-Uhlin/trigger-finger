package emi.uhl.triggerfinger.gameObjects

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import emi.uhl.triggerfinger.game.Game
import kotlin.math.roundToInt

/**
 * @author Emil Uhlin, EMUH0001
 * Draws a scrolling seamless background (used in FLappyMode)
 */
class SeamlessBackground(srcBitmap: Bitmap,
                         private val backgroundSpeed: Float): Component() {
	
	/**
	 * Scale srcBitmap so that it fills the screen vertically
	 */
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
	
	/**
	 * Draws required amount of images to fill the width of the screen (at most probably 2)
	 */
	override fun draw(canvas: Canvas, paint: Paint?) {
		for (x in backgroundX.roundToInt() until Game.screenWidth step backgroundImage.width) {
			canvas.drawBitmap(backgroundImage, x.toFloat(), 0f, paint)
		}
	}
}