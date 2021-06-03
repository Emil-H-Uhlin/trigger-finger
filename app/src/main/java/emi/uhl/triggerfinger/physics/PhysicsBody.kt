package emi.uhl.triggerfinger.physics

import emi.uhl.triggerfinger.game.Game
import emi.uhl.triggerfinger.gameObjects.Component
import emi.uhl.triggerfinger.math.Vector2

class PhysicsBody(var velocity: Vector2 = Vector2.zero,
                  var angleVelocity: Float = .0f,
                  var useGravity: Boolean = true,
                  var useDamping: Boolean = true,
                  var mass: Float = 1.0f,
                  var freezeX: Boolean = false,
                  var freezeY: Boolean = false): Component() {
	
	override fun update(deltaTime: Float) {
		if (useGravity) velocity += Physics.GRAVITY * deltaTime
		
		if (useDamping) {
			velocity += -velocity * Physics.DAMPING * Game.timeScale
			angleVelocity -= angleVelocity * Physics.DAMPING * Game.timeScale
		}
		
		val vel = Vector2(if (freezeX) 0f else velocity.x, if (freezeY) 0f else velocity.y)
		
		transform.position += vel * deltaTime
		transform.rotation += angleVelocity * deltaTime
	}
	
	fun addForce(force: Vector2) {
		velocity += force / mass
	}
	
	fun addAngleForce(amount: Float) {
		angleVelocity += amount
	}
}