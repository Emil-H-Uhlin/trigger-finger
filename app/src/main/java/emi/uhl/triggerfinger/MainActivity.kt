package emi.uhl.triggerfinger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Switch

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        findViewById<Button>(R.id.btn_play).setOnClickListener {
            val gameActivity = Intent(this, GameActivity::class.java).apply {
                putExtra("right_handed", findViewById<Switch>(R.id.tgl_right_handed).isChecked)
            }
    
            startActivity(gameActivity)
        }
    }
}