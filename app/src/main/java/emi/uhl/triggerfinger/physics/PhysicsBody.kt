package emi.uhl.triggerfinger.physics

import emi.uhl.triggerfinger.Component
import emi.uhl.triggerfinger.Game
import emi.uhl.triggerfinger.math.Vector2

class PhysicsBody(var velocity: Vector2 = Vector2.zero,
                  var angleVelocity: Float = .0f,
                  var useGravity: Boolean = true,
                  var useDampening: Boolean = true,
                  var mass: Float = 1.0f): Component() {
	override fun update(deltaTime: Float) {
		if (useGravity) velocity += Physics.GRAVITY * deltaTime
		
		if (useDampening) {
			velocity += -velocity * Physics.DAMPING * Game.timeScale
			angleVelocity -= angleVelocity * Physics.DAMPING * Game.timeScale
		}
		
		transform.position += velocity * deltaTime
		transform.rotation += angleVelocity * deltaTime
	}
	
	fun addForce(force: Vector2) {
		velocity += force / mass
	}
	
	fun addAngleForce(amount: Float) {
		angleVelocity += amount
	}
}