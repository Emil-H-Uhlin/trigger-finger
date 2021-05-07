package emi.uhl.triggerfinger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import emi.uhl.triggerfinger.game.EndlessMode
import emi.uhl.triggerfinger.game.GameMode

class GameActivity : AppCompatActivity() {
	private lateinit var game: GameMode
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		val frame = FrameLayout(this)
		game = EndlessMode(this)
		
		val buttonLayout = LinearLayout(this).apply {
			intent.extras?.getBoolean("right_handed", true)?.run {
				gravity = if (this) Gravity.BOTTOM or Gravity.START
				else Gravity.BOTTOM or Gravity.END
			}
			
			setPadding(150,0,150,75)
		}
		
		val reloadButton = Button(this).apply {
			text = "reload"
			width = 200
			height = 200
		}
		
		reloadButton.setOnClickListener {
			(game as? EndlessMode)?.run {
				performReload()
			}
		}
		
		buttonLayout.addView(reloadButton)
		
		frame.addView(game)
		frame.addView(buttonLayout)
		
		setContentView(frame)
	}
	
	override fun onResume() {
		super.onResume()
		
		game.resume()
	}
	
	override fun onPause() {
		super.onPause()
		
		game.pause()
	}
}