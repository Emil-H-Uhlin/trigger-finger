package emi.uhl.triggerfinger.game

import android.content.Context
import android.graphics.BitmapFactory
import emi.uhl.triggerfinger.GameObject

class FlappyMode(context: Context): GameMode(context) {
	override val gameObjects: ArrayList<GameObject>
	
	init {
		val opts = BitmapFactory.Options().apply { inScaled = false }
		
		
		gameObjects = arrayListOf()
	}
	
	override fun update(deltaTime: Float) {
		
		super.update(deltaTime)
	}
	
	override fun draw() {
	
	}
}