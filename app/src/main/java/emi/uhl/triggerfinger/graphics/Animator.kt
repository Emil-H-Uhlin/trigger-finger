package emi.uhl.triggerfinger.graphics

import emi.uhl.triggerfinger.gameObjects.Component

/**
 * @author Emil Uhlin, EMUH0001
 * Uses an Animation-instance in order to animate a Sprite-component belonging to the same game object
 */
class Animator: Component() {
	companion object {
		const val ANIMATION_FPS: Int = 24
	}
	
	lateinit var sprite: Sprite
	
	var animation: Animation? = null
		set(value) {
			field?.reset()
			field = value
		}
	
	override fun initialize() {
		sprite = getComponent()!!
	}
	
	override fun update(deltaTime: Float) = animation?.run {
		update(deltaTime)
		sprite.bitmap = frame
	}
}