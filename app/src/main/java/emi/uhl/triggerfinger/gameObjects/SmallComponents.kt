package emi.uhl.triggerfinger.gameObjects

import emi.uhl.triggerfinger.math.Vector2

/**
 * @author Emil Uhlin, EMUH0001
 * Handles world position and rotation of a game object
 */
data class Transform(var position: Vector2 = Vector2.zero, var rotation: Float = 0f): Component()