package com.example.onemusicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onemusicplayer.databinding.ActivitySongSelectionBinding

class SongSelectionActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySongSelectionBinding
    private lateinit var mAdapter : MusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivitySongSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backBtnSA.setOnClickListener { finish() }

        binding.selectionRec.apply {
            setItemViewCacheSize(10)
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@SongSelectionActivity)

            mAdapter = MusicAdapter(
                this@SongSelectionActivity,
               MainActivity.MusicListMA,selectionActivity = true
            )
            adapter = mAdapter
        }

        // for search view
        binding.searchViewSA.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean =
                true // this method will call when user will press enter button
            override fun onQueryTextChange(newText: String?): Boolean {
                MainActivity.musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (song in MainActivity.MusicListMA) {
                        if (song.title.lowercase().contains(userInput)) {
                            MainActivity.musicListSearch.add(song)
                        }
                    }
                    MainActivity.search = true
                    mAdapter.updatedMusicList(searchList = MainActivity.musicListSearch)
                }
                return true
            }
        })
    }
}