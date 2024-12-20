package com.minedeath.audiosys

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.AnimationUtils

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Get the views
        val splashLogo: ImageView = findViewById(R.id.splashLogo)
        val loadingText: TextView = findViewById(R.id.loadingText)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)

        // Apply fade-in animations
        val fadeInLogo = AnimationUtils.loadAnimation(this, R.anim.logo_fade_in)
        val fadeInProgress = AnimationUtils.loadAnimation(this, R.anim.progress_bar_fade_in)

        splashLogo.startAnimation(fadeInLogo)
        progressBar.startAnimation(fadeInProgress)

        // Get the target activity class from the Intent extras
        val targetActivity = intent.getSerializableExtra("targetActivity") as? Class<*> ?: HomeActivity::class.java

        // Delay of 3 seconds before starting the next activity
        Handler().postDelayed({
            // Transition to the target activity
            val intent = Intent(this, targetActivity)
            startActivity(intent)

            // Close SplashActivity so the user can't go back to it
            finish()

        }, 3000)  // 3000 milliseconds = 3 seconds
    }
}
