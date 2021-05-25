package emi.uhl.triggerfinger.gameObjects

import android.graphics.Canvas
import android.graphics.Paint
import emi.uhl.triggerfinger.graphics.DrawingLayer
import emi.uhl.triggerfinger.math.Vector2

/**
 * @author Emil Uhlin, EMUH0001
 * Base of GameObject/Component system
 *
 * Requires the use of GameObject.Builder() to instantiate a new game object
 */
class GameObject private constructor(private val name: String, private val drawingLayer: DrawingLayer = DrawingLayer.MIDDLE) {
	/**
	 * Used to build game objects when adding them to scene
	 */
	class Builder(name: String, drawingLayer: DrawingLayer = DrawingLayer.MIDDLE) {
		private val gameObject: GameObject = GameObject(name, drawingLayer)
		
		// set position and rotation of transform, return builder
		fun withTransform(position: Vector2 = Vector2.zero, rotation: Float = 0f) = apply {
			gameObject.transform.position = position
			gameObject.transform.rotation = rotation
		}
		
		// add component to game object, return builder
		fun withComponent(component: Component) = apply { gameObject.addComponent(component) }
		fun build(): GameObject = gameObject // get built game object
	}
	
	val components: ArrayList<Component> = ArrayList() // all components belonging to this game object
	val transform: Transform = Transform() // transform belonging to this game object
	
	var destroyed: Boolean = false // is the game object destroyed?
	
	/**
	 * Get component on game object, or null if it does not have one.
	 * Returns the same type as desired
	 */
	inline fun <reified componentType: Component> getComponent(): componentType? = components.find { component -> component is componentType } as componentType?
	
	/**
	 * Add component to game object
	 * returns false if not allowed by arraylist
	 * Initializes component on add, sequenced adding of components required (ei. required components need to be added first)
	 */
	fun addComponent(component: Component): Boolean {
		if (components.add(component)){
			component.gameObject = this
			component.initialize()
			
			return true
		}
		
		return false
	}
	
	/**
	 * Update all components belonging to this game object (if applicable)
	 */
	fun update(deltaTime: Float) = components.forEach { it.update(deltaTime) }
	
	/**
	 * Draw all components belonging to this game object (if applicable)
	 */
	fun draw(canvas: Canvas, paint: Paint?, layer: DrawingLayer) = if (layer == drawingLayer) components.forEach { it.draw(canvas, paint) } else null
	
	override fun toString(): String {
		var comps = ""
		
		components.forEach { comps += "  -${ it.javaClass.simpleName } \n" }
		
		return " \n$name:\n$comps"
	}
}