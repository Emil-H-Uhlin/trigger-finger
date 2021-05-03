package emi.uhl.triggerfinger.physics

import android.graphics.RectF
import emi.uhl.triggerfinger.Component
import emi.uhl.triggerfinger.math.Vector2

sealed class CollisionShape(val layerMask: Int, val isLayer: Int = 0): Component() {
	class CollisionCircle(val radius: Float,
	                      layerMask: Int, isLayer: Int = 0) : CollisionShape(layerMask, isLayer) {
		override fun collidesWith(circle: CollisionCircle): Boolean = Vector2.distance(transform.position, circle.transform.position) <= radius + circle.radius
		override fun collidesWith(rectangle: CollisionRectangle): Boolean = rectangle.collidesWith(this)
		override fun contains(point: Vector2): Boolean = Vector2.distance(transform.position, point) <= radius
		override fun contains(x: Float, y: Float): Boolean = contains(Vector2(x, y))
	}
	
	class CollisionRectangle(private var width: Float,
	                         private var height: Float,
	                         layerMask: Int, isLayer: Int = 0) : CollisionShape(layerMask, isLayer) {
		private val rect: RectF get() = RectF(
			transform.position.x - width / 2f,
			transform.position.y - height / 2f,
			width,
			height
		)
		
		override fun collidesWith(circle: CollisionCircle): Boolean {
			val circlePos = circle.transform.position
			val r = rect
			
			return if (r.contains(circlePos.x, circlePos.y))	true
			else {
				val x = circlePos.x.coerceIn(r.left..r.right)
				val y = circlePos.y.coerceIn(r.top..r.bottom)
				
				val dx = circlePos.x - x
				val dy = circlePos.y - y
				
				(dx * dx) + (dy * dy) < circle.radius * circle.radius
			}
		}
		
		override fun collidesWith(rectangle: CollisionRectangle): Boolean = rectangle.rect.intersect(rect)
		override fun contains(point: Vector2): Boolean = contains(point.x, point.y)
		override fun contains(x: Float, y: Float): Boolean = rect.contains(x, y)
	}
	
	abstract fun collidesWith(circle: CollisionCircle): Boolean
	abstract fun collidesWith(rectangle: CollisionRectangle): Boolean
	abstract fun contains(point: Vector2): Boolean
	abstract fun contains(x: Float, y: Float): Boolean
	
	fun collidesWith(shape: CollisionShape): Boolean = if (shape is CollisionCircle) collidesWith(shape) else collidesWith(shape as CollisionRectangle)
}