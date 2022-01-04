package com.example.onemusicplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.onemusicplayer.databinding.MusicViewBinding

class MusicAdapter(
    private val context: Context,
    private var musicList: ArrayList<Music>,
    private val playListDetail: Boolean = false,
    private val selectionActivity: Boolean = false
) : RecyclerView.Adapter<MusicAdapter.MyHolder>() {


    class MyHolder(binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNameMv
        val album = binding.songAlbumMv
        val imageView = binding.imageMv
        val songDuration = binding.songDurationMv
        val songPlayIm = binding.playImgMV
        val root = binding.root   // variable for accessing the root layout for activity move
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
            MusicViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        holder.songPlayIm.visibility = View.GONE
        holder.songDuration.visibility = View.VISIBLE
        holder.apply {
            title.text = musicList[position].title
            title.isSelected = true
            album.text = musicList[position].album

            if (PlayerActivity.songPlayBtn == position) {
                songDuration.visibility = View.GONE
                songPlayIm.visibility = View.VISIBLE

            }
            if (PlayerActivity.songPlayBtn != position) {
                songPlayIm.visibility = View.GONE
                songDuration.visibility = View.VISIBLE
                songDuration.text = forMateDuration(musicList[position].duration)
            }

            Glide.with(context)
                .load(musicList[position].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_palyer_icon).centerCrop())
                .into(imageView)

            when {
                playListDetail -> {
                    holder.root.setOnClickListener {
                        sendIntent(
                            ref = "PlayListDetailAdapter",
                            pos = position
                        )
                    }
                }
                selectionActivity -> {
                    holder.root.setOnClickListener {
                        if (addSong(musicList[position])) {
                            holder.root.setBackgroundColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.cool_pink
                                )
                            )
                        } else {
                            holder.root.setBackgroundColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.white
                                )
                            )
                        }
                    }
                }
                else -> {
                    root.setOnClickListener {
                        when {
                            MainActivity.search -> sendIntent(
                                ref = "MusicAdapterSearch",
                                pos = position
                            )
                            musicList[position].id == PlayerActivity.nowPlayingId -> {
                                sendIntent(ref = "NowPlaying", pos = PlayerActivity.songPosition)
                            }
                            else -> sendIntent(ref = "MusicAdapter", pos = position)
                        }
                    }
                }
            }
        }
    }

    private fun addSong(song: Music): Boolean {
        PlaylistActivity.musicPlaylist.ref[PlaylistDetail.currentPlaylistPosition].playlist.forEachIndexed { index, music ->
            if (song.id == music.id) {
                PlaylistActivity.musicPlaylist.ref[PlaylistDetail.currentPlaylistPosition].playlist.removeAt(
                    index
                )
                return false
            }
        }
        PlaylistActivity.musicPlaylist.ref[PlaylistDetail.currentPlaylistPosition].playlist.add(song)
        return true
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatedMusicList(searchList: ArrayList<Music>) {
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String, pos: Int) {
        val intent = Intent(context, PlayerActivity::class.java)

        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        startActivity(context, intent, null)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist() {
        musicList = ArrayList()
        musicList =
            PlaylistActivity.musicPlaylist.ref[PlaylistDetail.currentPlaylistPosition].playlist
        notifyDataSetChanged()
    }
}