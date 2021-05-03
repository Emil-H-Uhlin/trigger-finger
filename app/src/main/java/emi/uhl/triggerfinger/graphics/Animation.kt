package emi.uhl.triggerfinger.graphics

import android.graphics.Bitmap
import android.graphics.Rect

typealias AnimationEvent = () -> Unit

typealias Transition = (Any) -> Animation

data class Animation(val bitmap: Bitmap,
                     val frameCount: Int, val frameWidth: Int, val frameHeight: Int,
                     val rows: Int = 1, val columns: Int = frameCount, val firstFrameY: Int = 0) {
	companion object {
		const val FPS: Int = 24
		
		fun fromSprite(sprite: Sprite): Animation = Animation(sprite.bitmap, 1, sprite.bitmap.width, sprite.bitmap.height)
	}
	
	val transitions: ArrayList<Transition> = arrayListOf()
	
	var onAnimationEnd: AnimationEvent? = null
	
	private var frameIndex: Int = 0
	private var currentFrame: Int get() = frameIndex
		set(value) {
			val nextFrame = value % frameCount
			
			if (frameIndex!= 0 && nextFrame == 0) {
				onAnimationEnd?.invoke()
			} else frameIndex = nextFrame
		}
	
	private val column: Int get() = currentFrame % columns
	
	private val row: Int get() {
		for (row in 0..rows) {
			if (currentFrame in row * columns until row * columns + columns) return row
		}
		
		throw Exception("Invalid row index!").apply { printStackTrace() }
	}
	
	private val sourceRect: Rect
		get() = Rect(
			column * frameWidth,
			firstFrameY + row * frameHeight,
			column * frameWidth + frameWidth,
			firstFrameY + frameHeight + row * frameHeight
		)
	
	private var timer: Float = .0f
	
	val frame: Bitmap get() = Bitmap.createBitmap(bitmap, sourceRect.left, sourceRect.top, sourceRect.width(), sourceRect.height())
	
	fun update(deltaTime: Float) = if (frameCount > 1) run {
		timer += deltaTime
		
		if (timer > 1f / FPS) {
			currentFrame++
			timer = .0f
		}
	} else null
	
	fun reset() {
		frameIndex = 0
		timer = .0f
	}
}
