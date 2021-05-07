package emi.uhl.triggerfinger.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import emi.uhl.triggerfinger.*
import emi.uhl.triggerfinger.graphics.Animation
import emi.uhl.triggerfinger.graphics.Animator
import emi.uhl.triggerfinger.graphics.Sprite
import emi.uhl.triggerfinger.math.Vector2
import emi.uhl.triggerfinger.physics.CollisionShape
import emi.uhl.triggerfinger.physics.Physics
import emi.uhl.triggerfinger.physics.PhysicsBody
import kotlin.math.roundToInt

class EndlessMode(context: Context): GameMode(context) {
	override val gameObjects: ArrayList<GameObject>
	
	private val player: GameObject
	private val playerBehaviour: PlayerBehaviour
	
	private val lava: GameObject
	private val lavaBehaviour: LavaBehaviour
	
	private var worldPosition: Vector2
	
	private val uiTextPaint: Paint = Paint().apply {
		color = Color.WHITE
		textSize = 48f
	}
	
	private val gameOverPaint: Paint = Paint().apply {
		color = Color.BLACK
		textSize = 128f
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
		
		worldPosition = Vector2.down * resources.displayMetrics.heightPixels.toFloat() / 2f
	}
	
	override fun update(deltaTime: Float) {
		when (gameState) {
			GameState.PAUSED -> lavaBehaviour.updateOffset(deltaTime)
			
			GameState.GAME_OVER -> lava.update(deltaTime)
			
			GameState.PLAYING -> {
				gameObjects.forEach { it.update(deltaTime) }
				gameObjects.filter { it.destroyed }.forEach { gameObjects.remove(it) }
				
				val playerDisplayPosition = player.transform.position - -worldPosition
				
				if (player.transform.position.y > lava.transform.position.y) {
					gameState = GameState.GAME_OVER
				} else if (playerDisplayPosition.y < Game.screenHeight * 1f / 2.5f) {
					worldPosition.y -= playerDisplayPosition.y - Game.screenHeight * 1f / 2.5f
				}
				
				if (TouchEventHandler.isTouching) TouchEventHandler.onTouchHold()
			}
		}
		
		super.update(deltaTime)
	}
	
	override fun draw() {
		if (!holder.surface.isValid) return
		
		val canvas = holder.lockCanvas()
		canvas.drawColor(Color.BLACK)
		
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