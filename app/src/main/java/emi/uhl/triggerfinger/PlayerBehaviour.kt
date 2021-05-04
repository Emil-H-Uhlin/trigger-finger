package emi.uhl.triggerfinger

import emi.uhl.triggerfinger.game.Game
import emi.uhl.triggerfinger.graphics.Animation
import emi.uhl.triggerfinger.graphics.Animator
import emi.uhl.triggerfinger.graphics.Sprite
import emi.uhl.triggerfinger.math.Vector2
import emi.uhl.triggerfinger.physics.PhysicsBody
import kotlin.math.*

class PlayerBehaviour(var maxAmmo: Int,
                      val shootAnimation: Animation,
                      var remainingAmmo: Int = maxAmmo,
                      val reloadPenaltyTime: Float = .9f): Component() {
	lateinit var body: PhysicsBody
	lateinit var sprite: Sprite
	lateinit var animator: Animator
	
	var timer: Float = .0f
	val cooldown: Boolean get() = timer > .0f
	
	override fun initialize() {
		body = getComponent()!!
		sprite = getComponent()!!
		animator = getComponent()!!
	}
	
	fun shoot(quickShot: Boolean) {
		if (remainingAmmo < 1 || cooldown) return
		
		val dir = Vector2(cos(transform.rotation), sin(transform.rotation))
		body.addForce(-dir * if (quickShot) 1500f else 950f)
		
		body.angleVelocity = 4f * Math.PI.toFloat() * if (sprite.flipY) 1f else -1f
		
		remainingAmmo--
		
		animator.animation = shootAnimation
	}
	
	fun reload() {
		remainingAmmo = maxAmmo
		timer = reloadPenaltyTime
	}
	
	override fun update(deltaTime: Float) {
		if (timer > 0) timer -= deltaTime
		
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