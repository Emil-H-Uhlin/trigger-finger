package emi.uhl.triggerfinger.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import emi.uhl.triggerfinger.GameObject
import emi.uhl.triggerfinger.PlayerBehaviour
import emi.uhl.triggerfinger.R
import emi.uhl.triggerfinger.TouchEventHandler
import emi.uhl.triggerfinger.graphics.Animation
import emi.uhl.triggerfinger.graphics.Animator
import emi.uhl.triggerfinger.graphics.Sprite
import emi.uhl.triggerfinger.math.Vector2
import emi.uhl.triggerfinger.physics.CollisionShape
import emi.uhl.triggerfinger.physics.Physics
import emi.uhl.triggerfinger.physics.PhysicsBody

class FlappyMode(context: Context): GameMode(context) {
	override val gameObjects: ArrayList<GameObject>
	
	private val player: GameObject
	private val playerBehaviour: PlayerBehaviour
	
	private var worldPosition: Vector2
	
	init {
		val opts = BitmapFactory.Options().apply { inScaled = false }
		
		val playerSpriteSheet = BitmapFactory.decodeResource(resources,
			R.drawable.gun_player_shoot, opts)
		val gunSprite: Bitmap = Bitmap.createBitmap(playerSpriteSheet, 0, 0, 64, 48)
		
		val shootAnimation = Animation(playerSpriteSheet, 8, 64, 48)
		
		player = GameObject.Builder("Player")
			.withComponent(Sprite(gunSprite, scale = 4.5f))
			.withComponent(Animator())
			.withComponent(CollisionShape.CollisionCircle(30f, Physics.ENEMY, Physics.PLAYER))
			.withComponent(PhysicsBody())
			.withComponent(PlayerBehaviour(30, shootAnimation))
			.build()
		
		playerBehaviour = player.getComponent()!!
		
		TouchEventHandler.run {
			touchStartEvent.add { _, _ ->
				if (gameState == GameState.PLAYING)
					if (playerBehaviour.remainingAmmo > 0 && !playerBehaviour.cooldown)
						Game.timeScale = 0.4f
			}
			
			touchEndEvent.add { _, _ ->
				playerBehaviour.shoot(durationOfTouch < 0.2f && !wasDrag)
				Game.timeScale = 1.0f
			}
		}
		
		worldPosition = Vector2.left * resources.displayMetrics.widthPixels.toFloat() / 2f
		
		gameObjects = arrayListOf(player)
	}
	
	override fun update(deltaTime: Float) {
		when (gameState) {
			GameState.PLAYING -> {
				gameObjects.forEach { it.update(deltaTime) }
				gameObjects.filter { it.destroyed }.forEach { gameObjects.remove(it) }
				
				/*if (COLLISION WITH PIPES OR BELOW LEVEL) {
					gameState = GameState.GAME_OVER
				}*/
				
				if (TouchEventHandler.isTouching) TouchEventHandler.onTouchHold()
			}
		}
		
		super.update(deltaTime)
	}
	
	override fun draw() {
		if (!holder.surface.isValid) return
		
		val canvas = holder.lockCanvas()
		
		
		holder.unlockCanvasAndPost(canvas)
	}
}