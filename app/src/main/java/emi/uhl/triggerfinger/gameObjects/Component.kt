package emi.uhl.triggerfinger.gameObjects

import android.graphics.Canvas
import android.graphics.Paint

/**
 * @author Emil Uhlin, EMUH0001
 * Base of GameObject/Component system
 */
abstract class Component {
	lateinit var gameObject: GameObject
	val transform: Transform get() = gameObject.transform
	
	inline fun <reified type: Component> getComponent(): type? = gameObject.getComponent() // get component of belonging game object
	
	open fun initialize(): Unit? = null // initialize this component - used to get components on launch if needed
	open fun update(deltaTime: Float): Unit? = null // update this component - components may chose whether they need an update or not
	open fun draw(canvas: Canvas, paint: Paint?): Unit? = null // draw this component - components may chose whether they need a drawing method or not
}