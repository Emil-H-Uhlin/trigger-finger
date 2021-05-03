package emi.uhl.triggerfinger.graphics

import androidx.core.util.Predicate
import emi.uhl.triggerfinger.Component

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