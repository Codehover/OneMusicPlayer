package com.example.onemusicplayer


import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.onemusicplayer.databinding.ActivityPlaylistBinding
import com.example.onemusicplayer.databinding.AddPlaylistDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var playListAdapter: PlaylistAdapter

    companion object {
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtnPLA.setOnClickListener { finish() }
        binding.playlistRec.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = GridLayoutManager(this@PlaylistActivity, 2)
            playListAdapter = PlaylistAdapter(this@PlaylistActivity, musicPlaylist.ref)
            adapter = playListAdapter
        }
        binding.playlistAddBtnPLA.setOnClickListener {
            customAlertDialog()
        }
    }

    private fun customAlertDialog() {
        val customDialog = LayoutInflater.from(this@PlaylistActivity)
            .inflate(R.layout.add_playlist_dialog, binding.root, false)

        val binder = AddPlaylistDialogBinding.bind(customDialog)

        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(customDialog)
            .setTitle("Playlist Name")
            .setPositiveButton("ADD") { dialog, _ ->

                val playlistName = binder.playlistName.text
                val createdBy = binder.playlistUserName.text

                if (playlistName != null && createdBy != null) {
                    if (playlistName.isNotEmpty() && createdBy.isNotEmpty()) {
                        addPlaylist(playlistName.toString(), createdBy.toString())
                    }
                }
                dialog.dismiss()
            }.show()
    }

    private fun addPlaylist(playlistName:String,created:String){
        var playlistExist = false
        for (i in musicPlaylist.ref){
            if (playlistName == i.name ){
                playlistExist = true
                break
            }
        }
        if (playlistExist) Toast.makeText(this@PlaylistActivity,"Playlist Exist!!",Toast.LENGTH_SHORT).show()
        else {
            val tempPlaylist = Playlist()
            tempPlaylist.apply {
                name = playlistName
                playlist = ArrayList()
                createBy = created

                val calendar = Calendar.getInstance().time
                val sdf = SimpleDateFormat("dd mmm yyyy",Locale.ENGLISH)
                createOn =sdf.format(calendar)
                musicPlaylist.ref.add(tempPlaylist)
                playListAdapter.refreshPlaylist()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        playListAdapter.notifyDataSetChanged()
    }
}
