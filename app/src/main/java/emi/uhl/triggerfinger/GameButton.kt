package emi.uhl.triggerfinger

import android.graphics.RectF

typealias ClickEvent = (x: Float, y: Float) -> Unit

interface Clickable {
	class GameButton(override val rect: RectF) : Clickable {
		override val onClick: ArrayList<ClickEvent> = arrayListOf()
	}
	
	val onClick: ArrayList<ClickEvent>
	
	val rect: RectF
	
	fun onClick(x: Float, y: Float) = onClick.forEach { it.invoke(x, y) }
	fun contains(x: Float, y: Float) = rect.contains(x, y)
}