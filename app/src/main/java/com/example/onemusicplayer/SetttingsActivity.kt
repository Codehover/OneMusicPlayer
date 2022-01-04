package com.example.onemusicplayer

import android.app.AlertDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.onemusicplayer.databinding.ActivitySetttingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SetttingsActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySetttingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentThemeNav[MainActivity.themeIndex])
        binding = ActivitySetttingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Setting"
        when(MainActivity.themeIndex){
            0 -> binding.coolPinkTheme.setBackgroundColor(Color.YELLOW)
            1 -> binding.coolBlueTheme.setBackgroundColor(Color.YELLOW)
            2 -> binding.coolPurpleTheme.setBackgroundColor(Color.YELLOW)
            3-> binding.coolGreenTheme.setBackgroundColor(Color.YELLOW)
            4 -> binding.coolBlackTheme.setBackgroundColor(Color.YELLOW)
        }
        binding.coolPinkTheme.setOnClickListener { saveTheme(0) }
        binding.coolBlueTheme.setOnClickListener { saveTheme(1) }
        binding.coolPurpleTheme.setOnClickListener { saveTheme(2) }
        binding.coolGreenTheme.setOnClickListener { saveTheme(3) }
        binding.coolBlackTheme.setOnClickListener { saveTheme(4) }
        binding.versionName.text = setVersionName()

        binding.sortImgBtn.setOnClickListener {
            val menuList = arrayOf("Recently Added", "Song Title", "File Size")
            var currentSort = MainActivity.sortOrder
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Sorting")

                .setPositiveButton("Ok") { _, _ ->
                    val editor = getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                    editor.putInt("sortOrder",currentSort)
                    editor.apply()
                }
                .setSingleChoiceItems(menuList, currentSort){ _,which ->
                    currentSort = which
                }

            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)

        }
    }

    private fun saveTheme(index: Int){
        if (MainActivity.themeIndex != index){

            val editor = getSharedPreferences("THEMES", MODE_PRIVATE).edit()
            editor.putInt("themeIndex",index)
            editor.apply()

            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Apply Theme")
                .setMessage("Do you want to apply theme?")
                .setPositiveButton("Yes") { _, _ ->
                    exitApplication()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
    }

    private fun setVersionName() :String{
        return "Version Name: ${BuildConfig.VERSION_NAME}"
    }
}