package emi.uhl.triggerfinger.gameObjects

import emi.uhl.triggerfinger.math.Vector2

data class Transform(var position: Vector2 = Vector2.zero, var rotation: Float = 0f): Component()