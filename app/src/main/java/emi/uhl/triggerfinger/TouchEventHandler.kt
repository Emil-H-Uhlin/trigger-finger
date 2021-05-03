package emi.uhl.triggerfinger

import emi.uhl.triggerfinger.math.Vector2

/**
 * @author Emil Uhlin, EMUH0001
 */


typealias TouchEvent = (Float, Float) -> Unit
object TouchEventHandler {

    var durationOfTouch: Float = -1f ; private set
    var wasDrag = false ; private set
    var isTouching = false

    lateinit var startOfTouch: Vector2
    private lateinit var lastKnownTouch: Vector2

    private var touchStartTime: Long = -1

    var touchStartEvent: ArrayList<TouchEvent> = arrayListOf({ x, y -> // setup default start touch event
        touchStartTime = System.currentTimeMillis()

        wasDrag = false
        isTouching = true

        startOfTouch = Vector2(x, y)
        lastKnownTouch = startOfTouch
    })

    var touchDragEvent: ArrayList<TouchEvent> = arrayListOf({ x, y -> // setup default drag event
        if (Vector2.distance(Vector2(x, y), startOfTouch) > 15)
            wasDrag = true

        lastKnownTouch = Vector2(x, y)
    })

    var touchHoldEvent: ArrayList<TouchEvent> = arrayListOf({ _, _ ->
        durationOfTouch = (System.currentTimeMillis() - touchStartTime).toFloat() / 1000
    })

    var touchEndEvent: ArrayList<TouchEvent> = arrayListOf({ _, _ -> // setup default release event
        durationOfTouch = (System.currentTimeMillis() - touchStartTime).toFloat() / 1000
        isTouching = false
    })

    fun onTouchStart(x: Float, y: Float) = touchStartEvent.forEach { it(x, y) }
    fun onTouchDrag(x: Float, y: Float) = touchDragEvent.forEach { it(x, y) }
    fun onTouchHold() = touchHoldEvent.forEach { it(lastKnownTouch.x, lastKnownTouch.y) }
    fun onTouchEnd(x: Float, y: Float) = touchEndEvent.forEach { it(x, y) }
}