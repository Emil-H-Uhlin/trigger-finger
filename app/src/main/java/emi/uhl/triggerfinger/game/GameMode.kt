package emi.uhl.triggerfinger.game

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.SurfaceView
import emi.uhl.triggerfinger.gameObjects.GameObject

abstract class GameMode(context: Context): SurfaceView(context), Runnable {
	@Volatile private var running = false
	private lateinit var gameThread: Thread
	
	var score: Int = 0
	
	var prevFrameTime: Long = -1
	
	private val deltaTime: Float get() = (System.currentTimeMillis() - prevFrameTime).toFloat() / 1000
	private val scaledDeltaTime: Float get() = deltaTime * Game.timeScale
	
	protected var gameState: GameState = GameState.PAUSED
	
	lateinit var soundPool: SoundPool
	
	val uiTextPaint: Paint = Paint().apply {
		color = Color.WHITE
		textSize = 48f
	}
	
	val gameOverPaint: Paint = Paint().apply {
		color = Color.BLACK
		textSize = 128f
	}
	
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
	
	final override fun run() {
		while (running) {
			update(scaledDeltaTime)
			draw()
		}
	}
	
	open fun update(deltaTime: Float) {
		prevFrameTime = System.currentTimeMillis()
	}
	
	abstract fun draw()
	
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