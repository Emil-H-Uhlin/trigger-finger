package emi.uhl.triggerfinger.gameObjects

import android.graphics.Canvas
import android.graphics.Paint

abstract class Component {
	lateinit var gameObject: GameObject
	val transform: Transform get() = gameObject.transform
	
	inline fun <reified type: Component> getComponent(): type? = gameObject.getComponent()
	
	open fun initialize(): Unit? = null
	open fun update(deltaTime: Float): Unit? = null
	open fun draw(canvas: Canvas, paint: Paint?): Unit? = null
}