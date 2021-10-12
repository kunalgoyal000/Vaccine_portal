package com.kunalapps.vaccineFinder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        GlobalScope.launch {
            delay(2000)
            launchIntent()
        }
        }

    private fun launchIntent() {
        startActivity(Intent(this,MainActivity::class.java))
        overridePendingTransition(R.anim.slide_from_right, R.anim.slideout_from_left);
        finish()
    }
}