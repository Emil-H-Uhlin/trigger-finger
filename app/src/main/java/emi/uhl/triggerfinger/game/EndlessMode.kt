package emi.uhl.triggerfinger.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import emi.uhl.triggerfinger.*
import emi.uhl.triggerfinger.gameObjects.GameObject
import emi.uhl.triggerfinger.gameObjects.LavaBehaviour
import emi.uhl.triggerfinger.gameObjects.PlayerBehaviour
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

class EndlessMode(context: Context): GameMode(context) {
	override val gameObjects: ArrayList<GameObject>
	
	private val player: GameObject
	private val playerBehaviour: PlayerBehaviour
	
	private val lava: GameObject
	private val lavaBehaviour: LavaBehaviour
	
	private var worldPosition: Vector2
	
	private var maxHeight: Int = 0
	private var gainedScore: Int = 0
	
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
			.withComponent(PhysicsBody())
			.withComponent(PlayerBehaviour(
				maxAmmo = 30,
				quickshotModifier = 2f,
				shootAnimation = shootAnimation))
			.withTransform(
				position = Vector2(Game.screenWidth / 2f, 0),
				rotation = (Math.PI * 2 *  -3f/4f).toFloat())
			.build()
		
		playerBehaviour = player.getComponent()!!
		
		lava = GameObject.Builder("Lava")
			.withComponent(LavaBehaviour(225f, player.transform))
			.build().apply {
				transform.position = Vector2(0f, resources.displayMetrics.heightPixels.toFloat() * .2f)
			}
		
		lavaBehaviour = lava.getComponent()!!
		
		gameObjects = arrayListOf(player, lava)
		
		val shotEffect = soundPool.load(context, R.raw.shoot_effect, 1)
		
		TouchEventHandler.run {
			touchStartEvent.add { _, _ ->
				if (gameState == GameState.PLAYING)
					if (playerBehaviour.remainingAmmo > 0 && !playerBehaviour.cooldown)
						Game.timeScale = 0.4f
			}
			
			touchEndEvent.add { _, _ ->
				playerBehaviour.shoot(durationOfTouch < 0.2f && !wasDrag)
				soundPool.play(shotEffect, 1.0f, 1.0f, 1, 0, 1.0f)
				Game.timeScale = 1.0f
			}
		}
		
		worldPosition = Vector2.down * resources.displayMetrics.heightPixels.toFloat() / 2f
	}
	
	override fun update(deltaTime: Float) {
		when (gameState) {
			GameState.PAUSED -> lavaBehaviour.updateOffset(deltaTime)
			
			GameState.GAME_OVER -> lava.update(deltaTime)
			
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
				
				val playerDisplayPosition = player.transform.position - -worldPosition
				
				if (player.transform.position.y > lava.transform.position.y) {
					gameState = GameState.GAME_OVER
				} else if (playerDisplayPosition.y < Game.screenHeight * 1f / 2.5f) {
					worldPosition.y -= playerDisplayPosition.y - Game.screenHeight * 1f / 2.5f
				}
				
				if (TouchEventHandler.isTouching) TouchEventHandler.onTouchHold()
				
				val heightUnits = -Game.toUnits(player.transform.position.y).roundToInt()
				maxHeight = maxHeight.coerceAtLeast(heightUnits)
				
				score = maxHeight + gainedScore
			}
		}
		
		super.update(deltaTime)
	}
	
	override fun draw() {
		if (!holder.surface.isValid) return
		
		val canvas = holder.lockCanvas()
		canvas.drawColor(Color.rgb(110, 197, 233))
		
		canvas.save()
		canvas.translate(worldPosition.x, worldPosition.y)
		
		for (layer in DrawingLayer.values()) {
			for (i in 0 until gameObjects.count()) {
				gameObjects[i].draw(canvas, null, layer)
			}
		}
		
		canvas.restore()
		
		drawUI(canvas)
		
		holder.unlockCanvasAndPost(canvas)
	}
	
	private fun drawUI(canvas: Canvas) {
		when (gameState) {
			GameState.GAME_OVER -> {
				val gameOverText = "GAME OVER"
				canvas.drawText(gameOverText,
					(Game.screenWidth / 2 - gameOverPaint.measureText(gameOverText) / 2),
					(lava.transform.position.y + worldPosition.y + Game.screenHeight / 2f).coerceIn(
						(Game.screenHeight / 2f)..(Game.screenHeight + 300f)), gameOverPaint)
			}
			
			else -> {
				val heightText = "Height: ${ -(Game.toUnits(player.transform.position.y)).roundToInt() }"
				canvas.drawText(heightText, (Game.screenWidth / 2 - uiTextPaint.measureText(heightText) / 2), 50f, uiTextPaint)
				
				val ammoText = "${ playerBehaviour.remainingAmmo } / ${ playerBehaviour.maxAmmo }"
				canvas.drawText(ammoText, Game.screenWidth - uiTextPaint.measureText(ammoText), 50f, uiTextPaint)
			}
		}
		
		val scoreText = "Score: $score";
		canvas.drawText(scoreText, 0f, 50f, uiTextPaint)
	}
	
	fun performReload() = playerBehaviour.reload()
	
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