package emi.uhl.triggerfinger.gameObjects

import android.graphics.Bitmap
import emi.uhl.triggerfinger.game.FlappyMode
import emi.uhl.triggerfinger.game.Game
import emi.uhl.triggerfinger.graphics.Sprite
import emi.uhl.triggerfinger.math.Vector2
import emi.uhl.triggerfinger.physics.CollisionShape
import emi.uhl.triggerfinger.physics.Physics
import kotlin.random.Random

class PipeSpawner(private val flappyMode: FlappyMode,
                  private val spawnDistance: Float,
                  private val pipeSpeed: Float,
                  private val pipeSprite: Bitmap,
                  private val spaceTopBottom: Float = Game.toPixels(3f)): Component() {
	
	private val pipes = ArrayList<GameObject>()
	var totalMoved: Float = spawnDistance
	
	override fun update(deltaTime: Float) {
		pipes.forEach {
			it.transform.position += Vector2.left * pipeSpeed * deltaTime
			
			if (it.transform.position.x < -pipeSprite.width) {
				it.destroyed = true
			}
		}
		
		totalMoved += pipeSpeed * deltaTime
		
		for (i in pipes.count() - 1 downTo 0) {
			val pipe = pipes[i]
			
			if (pipe.destroyed) {
				pipes.remove(pipe)
			}
		}
		
		if (totalMoved > spawnDistance + pipeSprite.width) {
			createPipes()
			
			totalMoved = 0f;
		}
	}
	
	private fun createPipes() {
		val offset = Game.screenHeight * Random.nextFloat().coerceIn(.35f.. .65f)
		
		var sprite = Sprite(pipeSprite).apply {
			flipY = true
		}
		
		var shape = CollisionShape.CollisionRectangle(sprite.size.x, sprite.size.y, 0, Physics.ENEMY)
		
		var pipe = GameObject.Builder("Pipe top")
			.withComponent(sprite)
			.withComponent(shape)
			.build().apply {
				transform.position = Vector2(Game.screenWidth + sprite.origin.x, (offset - spaceTopBottom / 2f) - sprite.size.y)
			}
		
		pipes.add(pipe)
		flappyMode.addGameObject(pipe)
		
		sprite = Sprite(pipeSprite)
		shape = CollisionShape.CollisionRectangle(sprite.size.x, sprite.size.y, 0, Physics.ENEMY)
		
		pipe = GameObject.Builder("Pipe bottom")
			.withComponent(sprite)
			.withComponent(shape)
			.build().apply {
				transform.position = Vector2(Game.screenWidth + sprite.origin.x, (offset + spaceTopBottom / 2f))
			}
		
		pipes.add(pipe)
		flappyMode.addGameObject(pipe)
	}
}