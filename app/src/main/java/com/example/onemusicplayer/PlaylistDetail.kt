package com.example.onemusicplayer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.onemusicplayer.databinding.ActivityPlaylistDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

class PlaylistDetail : AppCompatActivity() {

   private lateinit var binding: ActivityPlaylistDetailBinding
    private lateinit var mAdapter: MusicAdapter

    companion object {
        var currentPlaylistPosition: Int = -1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityPlaylistDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentPlaylistPosition = intent.extras?.get("index") as Int
        PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition]
            .playlist = checkPlaylist(PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist)
        binding.playlistRecPD.apply {
            setItemViewCacheSize(10)
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@PlaylistDetail)
            mAdapter = MusicAdapter(
                this@PlaylistDetail,
                PlaylistActivity.musicPlaylist
                    .ref[currentPlaylistPosition]
                    .playlist,
                playListDetail = true
            )
            adapter = mAdapter
        }
        binding.backBtnPD.setOnClickListener { finish() }
        binding.shuffleBtnPD.setOnClickListener {
            val playerIntent = Intent(this@PlaylistDetail, PlayerActivity::class.java)
            playerIntent.putExtra("index", 0)
            playerIntent.putExtra("class", "PlaylistDetailShuffle")
            startActivity(playerIntent)
        }
        binding.addBtnPD.setOnClickListener {
            startActivity(Intent(this@PlaylistDetail, SongSelectionActivity::class.java))
        }
        binding.removeAllBtnPD.setOnClickListener {

            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Remove")
                .setMessage("Do you want to remove all songs from playlist?")
                .setPositiveButton("Yes") { dialog, _ ->
                    PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist.clear()
                    mAdapter.refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)

        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        binding.apply {
            playlistNamePD.text = PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].name
            moreSongNamePD.text = "Total ${mAdapter.itemCount} Songs. \n\n" +
                    "Created On:\n${PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].createOn}\n\n" +
                    "    -- ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].createBy} --"
        }
        if (mAdapter.itemCount > 0) {
            Glide.with(this@PlaylistDetail)
                .load(PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_palyer_icon).centerCrop())
                .into(binding.playListImgPD)
            binding.shuffleBtnPD.visibility = View.VISIBLE
        }
        mAdapter.notifyDataSetChanged()

        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("musicPlaylist",jsonStringPlaylist)
        editor.apply()
    }
}