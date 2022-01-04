package com.example.onemusicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.onemusicplayer.databinding.ActivityAboutBinding
import com.example.onemusicplayer.databinding.ActivityFeedbackBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "About"
       binding.aboutTxtAbout.text = aboutText()
    }

    private fun aboutText() : String{
        return "Developed By: CoolTech"
    }
}