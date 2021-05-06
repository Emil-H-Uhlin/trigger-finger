package emi.uhl.triggerfinger.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceView
import emi.uhl.triggerfinger.*
import emi.uhl.triggerfinger.graphics.Animation
import emi.uhl.triggerfinger.graphics.Animator
import emi.uhl.triggerfinger.graphics.Sprite
import emi.uhl.triggerfinger.math.Vector2
import emi.uhl.triggerfinger.physics.CollisionShape
import emi.uhl.triggerfinger.physics.Physics
import emi.uhl.triggerfinger.physics.PhysicsBody
import kotlin.math.roundToInt

class Game(context: Context): SurfaceView(context), Runnable {
	@Volatile var playing = false
	
	private lateinit var gameThread: Thread
	
	private var gameState: GameState = GameState.PAUSED
	
	private val player: GameObject
	private val playerBehaviour: PlayerBehaviour
	
	private val lava: GameObject
	private val lavaBehaviour: LavaBehaviour
	
	private var worldPosition: Vector2
	
	private val gameObjects: ArrayList<GameObject>
	
	private var prevFrameTime: Long = -1
	
	private val deltaTime: Float get() = (System.currentTimeMillis() - prevFrameTime).toFloat() / 1000
	private val scaledDeltaTime: Float get() = deltaTime * timeScale
	
	private val uiTextPaint: Paint = Paint().apply {
		color = Color.WHITE
		textSize = 48f
	}
	
	private val gameOverPaint: Paint = Paint().apply {
		color = Color.BLACK
		textSize = 128f
	}
	
	companion object {
		var screenHeight = -1
		var screenWidth = -1
		
		var timeScale: Float = 1.0f; private set
		
		private const val pixelsInUnit: Int = 250
		
		fun toUnits(value: Float): Float {
			return value / pixelsInUnit
		}
	}
	
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
		
		player.transform.apply {
			position = Vector2(resources.displayMetrics.widthPixels / 2f, 0f)
			rotation = (Math.PI * 2f * 1f/4f).toFloat()
		}
		
		lava = GameObject.Builder("Lava")
			.withComponent(LavaBehaviour(225f, player.transform))
			.build().apply {
				transform.position = Vector2(0f, resources.displayMetrics.heightPixels.toFloat() * .2f)
			}
		
		lavaBehaviour = lava.getComponent()!!
		
		gameObjects = arrayListOf(player, lava)
		
		gameObjects.forEach { println(it.toString()) }
		
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
	
	override fun run() {
		while (playing) {
			draw()
			update()
		}
	}
	
	private fun update() {
		val frameDelta = scaledDeltaTime
		
		when (gameState) {
			GameState.PAUSED -> lavaBehaviour.updateOffset(frameDelta)
			
			GameState.GAME_OVER -> lava.update(frameDelta)
			
			GameState.PLAYING -> {
				gameObjects.forEach { it.update(frameDelta) }
				gameObjects.filter { it.destroyed }.forEach { gameObjects.remove(it) }
				
				if (player.transform.position.y > lava.transform.position.y) {
					gameState = GameState.GAME_OVER
				}
				
				if (TouchEventHandler.isTouching) TouchEventHandler.onTouchHold()
			}
		}
		
		prevFrameTime = System.currentTimeMillis()
	}
	
	private fun draw() {
		if (!holder.surface.isValid) return
		
		val canvas = holder.lockCanvas()
		canvas.drawColor(Color.BLACK)
		
		if (screenWidth < 0 || screenHeight < 0){
			screenWidth = canvas.width
			screenHeight = canvas.height
		}
		
		val playerDisplayPosition = player.transform.position - -worldPosition
		
		if (playerDisplayPosition.y < screenHeight * 1f / 2.5f) {
			worldPosition.y -= playerDisplayPosition.y - screenHeight * 1f / 2.5f
		}
		
		canvas.save()
		canvas.translate(worldPosition.x, worldPosition.y)
		
		for (layer in DrawingLayer.values()) {
			gameObjects.forEach { it.draw(canvas, null, layer) }
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
					(screenWidth / 2 - gameOverPaint.measureText(gameOverText) / 2),
					(lava.transform.position.y + worldPosition.y + screenHeight / 2f).coerceIn(
						(screenHeight / 2f)..(screenHeight + 300f)), gameOverPaint)
			}
			
			else -> {
				val heightText = "Height: ${ -(toUnits(player.transform.position.y)).roundToInt() }"
				canvas.drawText(heightText, (screenWidth / 2 - uiTextPaint.measureText(heightText) / 2), 50f, uiTextPaint)
				
				val ammoText = "${ playerBehaviour.remainingAmmo } / ${ playerBehaviour.maxAmmo }"
				canvas.drawText(ammoText, screenWidth - uiTextPaint.measureText(ammoText), 50f, uiTextPaint)
			}
		}
	}
	
	fun resume() {
		playing = true
		prevFrameTime = System.currentTimeMillis()
		
		gameThread = Thread(this)
		gameThread.start()
	}
	
	fun pause() {
		playing = false
		
		try { gameThread.join() }
		catch(e: Exception) { println(e.stackTrace) }
	}
	
	fun performReload() = playerBehaviour.reload()
	
	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent?): Boolean {
		event?.run {
			val screenPoint = Vector2(event.x, event.y) - worldPosition
			val worldPoint = Vector2(event.x, event.y)
			
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