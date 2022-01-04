package com.example.onemusicplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onemusicplayer.databinding.ActivityFavouriteBinding

class FavouriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavouriteBinding
    private lateinit var favAdapter : FavouriteAdapter

    companion object{
        var favListSongs : ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityFavouriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /// this line code for back button on top left corner
        binding.backBtnFA.setOnClickListener { finish() }

        favListSongs = checkPlaylist(favListSongs)

        /// recycler code
        binding.favouriteRec.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = GridLayoutManager(this@FavouriteActivity,4)
            favAdapter = FavouriteAdapter(this@FavouriteActivity, favListSongs)
            adapter = favAdapter
        }

        if (favListSongs.size < 1) binding.shuffleBtnFA.visibility = View.INVISIBLE
        binding.shuffleBtnFA.setOnClickListener {
            val playerIntent = Intent(this@FavouriteActivity, PlayerActivity::class.java)
            playerIntent.putExtra("index", 0)
            playerIntent.putExtra("class", "FavouriteShuffle")
            startActivity(playerIntent)
        }

    }
}