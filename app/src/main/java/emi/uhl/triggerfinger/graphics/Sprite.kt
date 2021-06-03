package emi.uhl.triggerfinger.graphics

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import emi.uhl.triggerfinger.gameObjects.Component
import emi.uhl.triggerfinger.math.Vector2

class Sprite(var bitmap: Bitmap,
                  var scale: Float = 1.0f,
                  var flipX: Boolean = false,
                  var flipY: Boolean = false): Component() {
    
    val origin get() = Vector2(bitmap.width / 2f, bitmap.height / 2f) * scale
    private val degrees get() = Math.toDegrees(transform.rotation.toDouble()).toFloat() % 360
    
    val size: Vector2 get() = Vector2(bitmap.width, bitmap.height) * scale
    
    /**
     * Draw bitmap
     * Scales bitmap if needed (if X or Y is flipped - or both)
     */
    override fun draw(canvas: Canvas, paint: Paint?) {
        val xScale: Float = (if (flipX) -1f else 1f) * scale
        val yScale: Float = (if (flipY) -1f else 1f) * scale

        val scaledBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width * xScale).toInt(), (bitmap.height * yScale).toInt(), false)
        
        canvas.apply {
            save()
            rotate(degrees, transform.position.x, transform.position.y)
        }.drawBitmap(scaledBitmap, transform.position.x - origin.x, transform.position.y - origin.y, paint)
            .also {
            canvas.restore()
        }
    }
}