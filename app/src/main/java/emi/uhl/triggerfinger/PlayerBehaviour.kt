package emi.uhl.triggerfinger

import emi.uhl.triggerfinger.graphics.Animation
import emi.uhl.triggerfinger.graphics.Animator
import emi.uhl.triggerfinger.graphics.Sprite
import emi.uhl.triggerfinger.math.Vector2
import emi.uhl.triggerfinger.physics.PhysicsBody
import kotlin.math.*

class PlayerBehaviour(var maxAmmo: Int,
                      val shootAnimation: Animation,
                      var remainingAmmo: Int = maxAmmo): Component() {
	lateinit var body: PhysicsBody
	lateinit var sprite: Sprite
	lateinit var animator: Animator
	
	override fun initialize() {
		body = getComponent()!!
		sprite = getComponent()!!
		animator = getComponent()!!
	}
	
	fun shoot(quickShot: Boolean) {
		if (remainingAmmo < 1) return
		
		val dir = Vector2(cos(transform.rotation), sin(transform.rotation))
		body.addForce(-dir * if (quickShot) 1500f else 950f)
		
		body.angleVelocity = 4f * Math.PI.toFloat() * if (sprite.flipY) 1f else -1f
		
		remainingAmmo--
		
		animator.animation = shootAnimation
	}
	
	fun reload() {
		remainingAmmo = maxAmmo
	}
	
	override fun update(deltaTime: Float) {
		sprite.flipY = transform.position.x < Game.screenWidth / 2f
		
		var bounce = false
		
		if (transform.position.x > Game.screenWidth - sprite.bitmap.width) {
			transform.position.x = (Game.screenWidth - sprite.bitmap.width).toFloat()
			bounce = true
		}
		else if (transform.position.x < sprite.bitmap.width) {
			transform.position.x = sprite.bitmap.width.toFloat()
			bounce = true
		}
		
		if (bounce) {
			body.velocity.y *= 0.7f
			body.velocity.x *= -0.5f
			body.angleVelocity *= -0.5f
		}
	}
}