package emi.uhl.triggerfinger.game

import android.content.Context
import android.view.SurfaceView
import emi.uhl.triggerfinger.GameObject

abstract class GameMode(context: Context): SurfaceView(context), Runnable {
	@Volatile private var running = false
	private lateinit var gameThread: Thread
	
	var prevFrameTime: Long = -1
	
	private val deltaTime: Float get() = (System.currentTimeMillis() - prevFrameTime).toFloat() / 1000
	private val scaledDeltaTime: Float get() = deltaTime * Game.timeScale
	
	protected var gameState: GameState = GameState.PAUSED
	
	abstract val gameObjects: ArrayList<GameObject>
	
	init {
		Game.screenWidth = resources.displayMetrics.widthPixels
		Game.screenHeight = resources.displayMetrics.heightPixels
	}
	
	companion object {
		private const val pixelsInUnit: Int = 250
		
		fun toUnits(value: Float): Float {
			return value / pixelsInUnit
		}
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