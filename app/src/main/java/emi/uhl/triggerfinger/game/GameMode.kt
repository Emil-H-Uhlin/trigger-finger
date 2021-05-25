package emi.uhl.triggerfinger.game

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.SurfaceView
import emi.uhl.triggerfinger.gameObjects.GameObject

/**
 * @author Emil Uhlin, EMUH0001
 * Handles runtime of some game mode inherited from this
 */
abstract class GameMode(context: Context): SurfaceView(context), Runnable {
	@Volatile private var running = false
	private lateinit var gameThread: Thread
	
	var score: Int = 0
	
	var prevFrameTime: Long = -1
	
	private val deltaTime: Float get() = (System.currentTimeMillis() - prevFrameTime).toFloat() / 1000 // time since last frame in seconds
	private val scaledDeltaTime: Float get() = deltaTime * Game.timeScale // scaled delta time using time scale of game
	
	protected var gameState: GameState = GameState.PAUSED // state of game
	
	val soundPool: SoundPool // used to play sound effects
	
	/**
	 * Paint used for drawing general UI-elements
	 */
	val uiTextPaint: Paint = Paint().apply {
		color = Color.WHITE
		textSize = 48f
	}
	
	/**
	 * Paint mainly used for drawing "GAME OVER" when game is over
	 */
	val gameOverPaint: Paint = Paint().apply {
		color = Color.BLACK
		textSize = 128f
	}
	
	// game objects in scene
	abstract val gameObjects: ArrayList<GameObject>
	
	init {
		Game.screenWidth = resources.displayMetrics.widthPixels
		Game.screenHeight = resources.displayMetrics.heightPixels
		
		val attributes = AudioAttributes.Builder()
			.setUsage(AudioAttributes.USAGE_GAME)
			.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
			.build()
		
		soundPool = SoundPool.Builder()
			.setMaxStreams(6)
			.setAudioAttributes(attributes)
			.build()
	}
	
	/**
	 * Executes main game loop using java Thread (should be replaced with kotlin coroutine)
	 */
	final override fun run() {
		while (running) {
			update(scaledDeltaTime)
			draw()
		}
	}
	
	/**
	 * Updates deltatime (use super.update(..) to get correct deltatime every frame)
	 */
	open fun update(deltaTime: Float) {
		prevFrameTime = System.currentTimeMillis()
	}
	
	abstract fun draw()
	
	/**
	 * Adds a new game object to the scene
	 */
	fun addGameObject(gameObject: GameObject) {
		gameObjects.add(gameObject)
	}
	
	fun resume() {
		running = true
		prevFrameTime = System.currentTimeMillis()
		
		gameThread = Thread(this)
		gameThread.start()
	}
	
	fun pause() {
		running = false
		
		try { gameThread.join() }
		catch (exception: Exception) { println("Error joining thread " + exception.printStackTrace()) }
	}
}