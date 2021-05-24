package emi.uhl.triggerfinger.game

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.SurfaceView
import emi.uhl.triggerfinger.gameObjects.GameObject

abstract class GameMode(context: Context): SurfaceView(context), Runnable {
	@Volatile private var running = false
	private lateinit var gameThread: Thread
	
	protected var score: Int = 0
	
	var prevFrameTime: Long = -1
	
	private val deltaTime: Float get() = (System.currentTimeMillis() - prevFrameTime).toFloat() / 1000
	private val scaledDeltaTime: Float get() = deltaTime * Game.timeScale
	
	protected var gameState: GameState = GameState.PAUSED
	
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