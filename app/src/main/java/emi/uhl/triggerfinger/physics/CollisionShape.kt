package emi.uhl.triggerfinger.physics

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import emi.uhl.triggerfinger.gameObjects.Component
import emi.uhl.triggerfinger.gameObjects.GameObject
import emi.uhl.triggerfinger.math.Vector2

typealias CollisionEvent = (GameObject) -> Unit

/**
 * @author Emil Uhlin, EMUH0001
 * Collider-objects used for simple intersect-logic
 */
sealed class CollisionShape(val layerMask: Int,
                            val isLayer: Int = 0): Component() {
	val onCollision: ArrayList<CollisionEvent> = arrayListOf()
	
	// invoke any and all collision events
	fun onCollision(gameObject: GameObject) = onCollision.forEach { it.invoke(gameObject) }
	
	abstract fun collidesWith(circle: CollisionCircle): Boolean
	abstract fun collidesWith(rectangle: CollisionRectangle): Boolean
	abstract fun contains(point: Vector2): Boolean
	abstract fun contains(x: Float, y: Float): Boolean
	
	fun collidesWith(shape: CollisionShape): Boolean = if (shape is CollisionCircle) collidesWith(shape) else collidesWith(shape as CollisionRectangle)
	
	class CollisionCircle(val radius: Float,
	                      layerMask: Int,
	                      isLayer: Int = 0): CollisionShape(layerMask, isLayer) {
		
		override fun collidesWith(circle: CollisionCircle): Boolean {
			return if (layerMask == layerMask or circle.isLayer)
				Vector2.distance(transform.position, circle.transform.position) <= radius + circle.radius
			else false
		}
		
		override fun collidesWith(rectangle: CollisionRectangle): Boolean {
			if (layerMask != layerMask or rectangle.isLayer) return false
			
			val circlePos = transform.position
			val r = rectangle.rect
			
			return if (r.contains(circlePos.x, circlePos.y)) true
			else {
				val x = circlePos.x.coerceIn(r.left..r.right)
				val y = circlePos.y.coerceIn(r.top..r.bottom)
					
				val dx = circlePos.x - x
				val dy = circlePos.y - y
					
				(dx * dx) + (dy * dy) < radius * radius
			}
		}
		
		override fun contains(point: Vector2): Boolean = Vector2.distance(transform.position, point) <= radius
		override fun contains(x: Float, y: Float): Boolean = contains(Vector2(x, y))
		
		override fun draw(canvas: Canvas, paint: Paint?) {
			canvas.drawCircle(transform.position.x, transform.position.y, radius, Paint().apply { color = Color.GREEN; style = Paint.Style.STROKE })
		}
	}
	
	class CollisionRectangle(private var width: Float,
	                         private var height: Float,
	                         layerMask: Int,
	                         isLayer: Int = 0): CollisionShape(layerMask, isLayer) {
		
		val rect: RectF get() = RectF(
			transform.position.x - width / 2f,
			transform.position.y - height / 2f,
			(transform.position.x - width / 2f) + width,
			(transform.position.y - height / 2f) + height
		)
		
		override fun collidesWith(circle: CollisionCircle): Boolean {
			if (layerMask != layerMask or circle.isLayer) return false
			
			val circlePos = circle.transform.position
			val r = rect
			
			return if (r.contains(circlePos.x, circlePos.y)) true
			else {
				val x = circlePos.x.coerceIn(r.left..r.right)
				val y = circlePos.y.coerceIn(r.top..r.bottom)
				
				val dx = circlePos.x - x
				val dy = circlePos.y - y
				
				(dx * dx) + (dy * dy) < circle.radius * circle.radius
			}
		}
		
		override fun collidesWith(rectangle: CollisionRectangle): Boolean {
			return if (layerMask == layerMask or rectangle.isLayer)
				rectangle.rect.intersect(rect)
			else false
		}
		
		override fun contains(point: Vector2): Boolean = contains(point.x, point.y)
		override fun contains(x: Float, y: Float): Boolean = rect.contains(x, y)
		
		override fun draw(canvas: Canvas, paint: Paint?) {
			canvas.drawRect(rect, Paint().apply { color = Color.RED; style = Paint.Style.STROKE })
		}
	}
}