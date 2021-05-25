package emi.uhl.triggerfinger.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.view.MotionEvent
import emi.uhl.triggerfinger.gameObjects.GameObject
import emi.uhl.triggerfinger.gameObjects.PlayerBehaviour
import emi.uhl.triggerfinger.R
import emi.uhl.triggerfinger.TouchEventHandler
import emi.uhl.triggerfinger.gameObjects.PipeSpawner
import emi.uhl.triggerfinger.gameObjects.SeamlessBackground
import emi.uhl.triggerfinger.graphics.Animation
import emi.uhl.triggerfinger.graphics.Animator
import emi.uhl.triggerfinger.graphics.DrawingLayer
import emi.uhl.triggerfinger.graphics.Sprite
import emi.uhl.triggerfinger.math.Vector2
import emi.uhl.triggerfinger.physics.CollisionShape
import emi.uhl.triggerfinger.physics.Physics
import emi.uhl.triggerfinger.physics.PhysicsBody
import kotlin.math.max
import kotlin.math.roundToInt

class FlappyMode(context: Context): GameMode(context) {
	override val gameObjects: ArrayList<GameObject>
	
	private val player: GameObject
	private val playerBehaviour: PlayerBehaviour
	private val pipeSpawner: PipeSpawner
	
	private var worldPosition: Vector2
	
	init {
		val opts = BitmapFactory.Options().apply { inScaled = false }
		
		val playerSpriteSheet = BitmapFactory.decodeResource(resources,
			R.drawable.gun_player_shoot, opts)
		
		val gunSprite: Bitmap = Bitmap.createBitmap(playerSpriteSheet, 0, 0, 64, 48)
		
		val shootAnimation = Animation(playerSpriteSheet, 8, 64, 48)
		
		val playerSprite = Sprite(gunSprite, scale = 4.5f).apply { flipY = true }
		
		player = GameObject.Builder("Player")
			.withComponent(playerSprite)
			.withComponent(Animator())
			.withComponent(CollisionShape.CollisionCircle(max(playerSprite.size.x, playerSprite.size.y) / 2f - 50, Physics.ENEMY, Physics.PLAYER).apply {
				onCollision.add {
					gameState = GameState.GAME_OVER
				}
			})
			.withComponent(PhysicsBody(freezeX = true))
			.withComponent(PlayerBehaviour(
				maxAmmo = 99999,
				shootForce = 1200f,
				quickshotModifier = 1.0f,
				shootAnimation = shootAnimation))
			.withTransform(
				position = Vector2(300, Game.screenHeight / 2),
				rotation = (Math.PI - Math.PI * 0.3).toFloat())
			.build()
		
		playerBehaviour = player.getComponent()!!
		
		worldPosition = Vector2.left * resources.displayMetrics.widthPixels.toFloat() / 2f
		
		val pipeSprite = BitmapFactory.decodeResource(resources, R.drawable.pipe, opts);
		
		val spawner = GameObject.Builder("Pipe-spawner")
			.withComponent(PipeSpawner(
				flappyMode = this,
				spawnDistance = 600f,
				pipeSpeed = 275f,
				pipeSprite = pipeSprite,
				spaceTopBottom = Game.toPixels(4f)))
			.build()
		
		pipeSpawner = spawner.getComponent()!!
		
		val background = GameObject.Builder("Seamless background", DrawingLayer.BACKGROUND)
			.withComponent(SeamlessBackground(BitmapFactory.decodeResource(resources, R.drawable.sunsetbackground, opts), 150f))
			.build()
		
		gameObjects = arrayListOf(player, spawner, background,)
		
		val shotEffect = soundPool.load(context, R.raw.shoot_effect, 1)
		
		TouchEventHandler.run {
			touchStartEvent.add { _, _ ->
				if (gameState == GameState.PLAYING)
					if (playerBehaviour.remainingAmmo > 0 && !playerBehaviour.cooldown)
						Game.timeScale = 0.6f
			}
			
			touchEndEvent.add { _, _ ->
				playerBehaviour.shoot(durationOfTouch < 0.2f && !wasDrag)
				soundPool.play(shotEffect, 1.0f, 1.0f, 1, 0, 1.0f)
				Game.timeScale = 1.0f
			}
		}
	}
	
	override fun update(deltaTime: Float) {
		when (gameState) {
			GameState.PLAYING -> {
				for (i in 0 until gameObjects.count()) {
					gameObjects[i].update(deltaTime)
				}
				
				for (i in 0 until gameObjects.count() - 1) {
					val collider = gameObjects[i].getComponent<CollisionShape>() ?: continue
					
					for (j in i until gameObjects.count()) {
						val other = gameObjects[j].getComponent<CollisionShape>() ?: continue
						
						if (collider.collidesWith(other)) {
							collider.onCollision(other.gameObject)
						}
						
						if (other.collidesWith(collider)) {
							other.onCollision(other.gameObject)
						}
					}
				}
				
				for (i in gameObjects.count() - 1 downTo 0) {
					val gameObject = gameObjects[i]
					
					if (gameObject.destroyed) {
						gameObjects.remove(gameObject)
					}
				}
				
				if (player.transform.position.y > Game.screenHeight) {
					gameState = GameState.GAME_OVER
				}
				
				if (TouchEventHandler.isTouching) TouchEventHandler.onTouchHold()
			}
			
			GameState.GAME_OVER -> {
				player.transform.rotation += 20f * deltaTime
				player.transform.position += Vector2.down * Game.screenHeight.toFloat() * deltaTime
			}
			
			else -> {
			
			}
		}
		
		super.update(deltaTime)
	}
	
	
	override fun draw() {
		if (!holder.surface.isValid) return
		
		val canvas = holder.lockCanvas()
		canvas.drawColor(Color.BLACK)
		
		for (layer in DrawingLayer.values()) {
			for (i in 0 until gameObjects.count()) {
				gameObjects[i].draw(canvas, null, layer)
			}
		}
		
		drawUI(canvas)
		
		holder.unlockCanvasAndPost(canvas)
	}
	
	private fun drawUI(canvas: Canvas) {
		if (gameState == GameState.GAME_OVER) {
			val gameOverText = "GAME OVER"
			canvas.drawText(gameOverText, Game.screenWidth / 2 - gameOverPaint.measureText(gameOverText) / 2, Game.screenHeight / 2f, gameOverPaint)
		}
		
		val scoreText = "Score: $score";
		canvas.drawText(scoreText, 0f, 50f, uiTextPaint)
	}
	
	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		event?.run {
			val screenPoint = Vector2(event.x, event.y) - worldPosition
			
			when (action) {
				MotionEvent.ACTION_DOWN -> {
					if (gameState == GameState.PAUSED) gameState = GameState.PLAYING
					
					TouchEventHandler.onTouchStart(
						screenPoint.x,
						screenPoint.y
					)
					
					return true
				}
				
				MotionEvent.ACTION_MOVE -> {
					TouchEventHandler.onTouchDrag(
						screenPoint.x,
						screenPoint.y
					)
					return true
				}
				
				MotionEvent.ACTION_UP -> {
					TouchEventHandler.onTouchEnd(
						screenPoint.x,
						screenPoint.y
					)
					return true
				}
				else -> return false
			}
		}
		
		return super.onTouchEvent(event)
	}
}