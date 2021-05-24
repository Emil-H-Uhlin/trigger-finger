package emi.uhl.triggerfinger.gameObjects

import android.graphics.Canvas
import android.graphics.Paint
import emi.uhl.triggerfinger.graphics.DrawingLayer
import emi.uhl.triggerfinger.math.Vector2

class GameObject private constructor(private val name: String, private val drawingLayer: DrawingLayer = DrawingLayer.MIDDLE) {
	class Builder(name: String) {
		private val gameObject: GameObject = GameObject(name)
		
		fun withTransform(position: Vector2 = Vector2.zero, rotation: Float = 0f) = apply {
			gameObject.transform.position = position
			gameObject.transform.rotation = rotation
		}
		
		fun withComponent(component: Component) = apply { gameObject.addComponent(component) }
		fun build(): GameObject = gameObject
	}
	
	val components: ArrayList<Component> = ArrayList()
	val transform: Transform = Transform()
	
	var destroyed: Boolean = false
	
	inline fun <reified componentType: Component> getComponent(): componentType? = components.find { component -> component is componentType } as componentType?
	
	fun addComponent(component: Component): Boolean {
		if (components.add(component)){
			component.gameObject = this
			component.initialize()
			
			return true
		}
		
		return false
	}
	
	fun update(deltaTime: Float) = components.forEach { it.update(deltaTime) }
	fun draw(canvas: Canvas, paint: Paint?, layer: DrawingLayer) = if (layer == drawingLayer) components.forEach { it.draw(canvas, paint) } else null
	
	override fun toString(): String {
		var comps = ""
		
		components.forEach { comps += "  -${ it.javaClass.simpleName } \n" }
		
		return " \n$name:\n$comps"
	}
}