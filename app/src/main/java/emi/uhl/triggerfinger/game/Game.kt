package emi.uhl.triggerfinger.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceView
import emi.uhl.triggerfinger.R
import emi.uhl.triggerfinger.TouchEventHandler
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
import kotlinx.coroutines.*
import java.math.BigDecimal
import kotlin.math.round

class Game(context: Context): SurfaceView(context) {
	@Volatile private var running = false
	
	val gameObjects: ArrayList<GameObject>
	
	private val player: GameObject
	private val playerBehaviour: PlayerBehaviour
	
	private val lava: GameObject
	private val lavaBehaviour: LavaBehaviour
	
	private var worldPosition: Vector2
	
	private var maxHeight: Int = 0
	private var gainedScore: Int = 0
	private var score: Int = 0
	
	var prevFrameTime: Long = -1
	
	private val deltaTime: Float get() = (System.currentTimeMillis() - prevFrameTime).toFloat() / 1000 // time since last frame in seconds
	private val scaledDeltaTime: Float get() = deltaTime * timeScale // scaled delta time using time scale of game
	
	var gameState: GameState = GameState.PAUSED // state of game
	
	/**
	 * Paint used for drawing general UI-elements
	 */
	private val uiTextPaint: Paint = Paint().apply {
		color = Color.WHITE
		textSize = 48f
	}
	
	/**
	 * Paint mainly used for drawing "GAME OVER" when game is over
	 */
	private val gameOverPaint: Paint = Paint().apply {
		color = Color.BLACK
		textSize = 128f
	}
	
	companion object {
		@JvmStatic var screenWidth: Int = -1
		@JvmStatic var screenHeight: Int = -1
		
		@JvmStatic var timeScale: Float = 1.0f
		
		val debugging: Boolean = false
	}
	
	init {
		screenWidth = resources.displayMetrics.widthPixels
		screenHeight = resources.displayMetrics.heightPixels
		
		val opts = BitmapFactory.Options().apply { inScaled = false } // unscaled sprites preferred
		
		val playerSpriteSheet = BitmapFactory.decodeResource(resources,
			R.drawable.gun_player_shoot, opts)
		
		val gunSprite: Bitmap = Bitmap.createBitmap(playerSpriteSheet, 0, 0, 64, 48)
		val shootAnimation = Animation(playerSpriteSheet, 8, 64, 48)
		val playerSprite = Sprite(gunSprite, scale = 4.5f).apply { flipY = true }
		
		player = GameObject.Builder("Player") // build player object
			.withComponent(playerSprite)
			.withComponent(Animator())
			.withComponent(
				CollisionShape.CollisionCircle(
					max(playerSprite.size.x, playerSprite.size.y) / 2f - 50,
					Physics.ENEMY,
					Physics.PLAYER).apply {
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
				position = Vector2(screenWidth / 2f, 0),
				rotation = (Math.PI * 2 *  -3f/4f).toFloat())
			.build()
		
		playerBehaviour = player.getComponent()!! // store player behaviour for easy access
		
		lava = GameObject.Builder("Lava") // build lava object
			.withComponent(LavaBehaviour(225f, player.transform))
			.withTransform(
				position = Vector2(0f, resources.displayMetrics.heightPixels.toFloat() * .2f)
			)
			.build()
		
		lavaBehaviour = lava.getComponent()!! // store lava behaviour for easy access
		
		gameObjects = arrayListOf(player, lava) // initialize list of game objects
		
		TouchEventHandler.run {
			touchStartEvent.add { _, _ ->
				if (gameState == GameState.PLAYING)
					if (playerBehaviour.remainingAmmo > 0 && !playerBehaviour.cooldown)
						timeScale = 0.4f
			}
			
			touchEndEvent.add { _, _ ->
				playerBehaviour.shoot(durationOfTouch < 0.2f && !wasDrag)
				
				timeScale = 1.0f
			}
		}
		
		worldPosition = Vector2.down * resources.displayMetrics.heightPixels.toFloat() / 2f
	}
	
	private fun update(deltaTime: Float = scaledDeltaTime) {
		when (gameState) {
			GameState.PAUSED -> lavaBehaviour.updateOffset(deltaTime) // render lava movement even when paused
			
			GameState.GAME_OVER -> lava.update(deltaTime) // update lava when game over
			
			GameState.PLAYING -> {
				// safely update all game objects that were present at the start of the scene
				for (i in 0 until gameObjects.count()) {
					gameObjects[i].update(deltaTime)
				}
				
				// check collision between each pair of game objects
				for (i in 0 until gameObjects.count() - 1) {
					val collider = gameObjects[i].getComponent<CollisionShape>() ?: continue // get collider or continue if null
					
					for (j in i until gameObjects.count()) {
						val other = gameObjects[j].getComponent<CollisionShape>() ?: continue // get collider or continue if null
						
						if (collider.collidesWith(other)) {
							collider.onCollision(other.gameObject)
						}
						
						if (other.collidesWith(collider)) {
							other.onCollision(other.gameObject)
						}
					}
				}
				
				// safely remove destroyed game objects
				for (i in gameObjects.count() - 1 downTo 0) {
					val gameObject = gameObjects[i]
					
					if (gameObject.destroyed) {
						gameObjects.remove(gameObject)
					}
				}
				
				val playerDisplayPosition = player.transform.position - -worldPosition // player position on screen
				
				if (player.transform.position.y > lava.transform.position.y) { // game over if gun falls into lava
					gameState = GameState.GAME_OVER
				} else if (playerDisplayPosition.y < screenHeight * 1f / 2.5f) { // move world upward with player
					worldPosition.y -= playerDisplayPosition.y - screenHeight * 1f / 2.5f
				}
				
				if (TouchEventHandler.isTouching) TouchEventHandler.onTouchHold()
				
				val heightUnits = -(player.transform.position.y / 200f).roundToInt()
				maxHeight = maxHeight.coerceAtLeast(heightUnits)
				
				score = maxHeight + gainedScore
			}
		}
		
		prevFrameTime = System.currentTimeMillis()
	}
	
	private fun draw() {
		if (!holder.surface.isValid) return
		
		val canvas = holder.lockCanvas()
		canvas.drawColor(Color.rgb(110, 197, 233))
		
		canvas.save()
		canvas.translate(worldPosition.x, worldPosition.y)
		
		// draw all game objects in order of their drawing layers
		for (layer in DrawingLayer.values()) {
			for (i in 0 until gameObjects.count()) {
				gameObjects[i].draw(canvas, null, layer)
			}
		}
		
		canvas.restore() // restore position of canvas for UI
		
		drawUI(canvas) // draw UI
		
		holder.unlockCanvasAndPost(canvas) // render frame
	}
	
	private fun drawUI(canvas: Canvas) {
		when (gameState) {
			GameState.GAME_OVER -> {
				val gameOverText = "GAME OVER"
				canvas.drawText(gameOverText,
					(screenWidth / 2 - gameOverPaint.measureText(gameOverText) / 2),
					(lava.transform.position.y + worldPosition.y + screenHeight / 2f).coerceIn(
						(screenHeight / 2f)..(screenHeight + 300f)), gameOverPaint)
			}
			
			else -> {
				val heightText = "Height: ${ -(player.transform.position.y / 200f).roundToInt() }"
				canvas.drawText(heightText, (screenWidth / 2 - uiTextPaint.measureText(heightText) / 2), 50f, uiTextPaint)
				
				val ammoText = "${ playerBehaviour.remainingAmmo } / ${ playerBehaviour.maxAmmo }"
				canvas.drawText(ammoText, screenWidth - uiTextPaint.measureText(ammoText), 50f, uiTextPaint)
			}
		}
		
		val scoreText = "Score: $score";
		canvas.drawText(scoreText, 0f, 50f, uiTextPaint)
		
		if (debugging) {
			val fps = 1.0f / (scaledDeltaTime / timeScale)
			
			val fpsText = "${fps.toBigDecimal().setScale(2, BigDecimal.ROUND_HALF_EVEN)} fps"
			canvas.drawText(fpsText, screenWidth - uiTextPaint.measureText(fpsText), 200f, uiTextPaint)
		}
	}
	
	/**
	 * Adds a new game object to the scene
	 */
	fun addGameObject(gameObject: GameObject) {
		gameObjects.add(gameObject)
	}
	
	fun performReload() = playerBehaviour.reload()
	
	@DelicateCoroutinesApi fun resume() = GlobalScope.launch {
		running = true
		
		prevFrameTime = System.currentTimeMillis()
		while (running) {
			update()
			draw()
		}
	}
	
	fun pause() {
		running = false
	}
	
	@SuppressLint("ClickableViewAccessibility") override fun onTouchEvent(event: MotionEvent?): Boolean {
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