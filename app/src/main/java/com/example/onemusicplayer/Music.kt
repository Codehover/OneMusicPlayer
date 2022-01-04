package com.example.onemusicplayer

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Long = 0,
    val path: String,  // this is file path of audio
    val artUri: String
)

class Playlist {
    lateinit var name: String
    lateinit var playlist: ArrayList<Music>
    lateinit var createBy: String
    lateinit var createOn: String
}

class MusicPlaylist {
    var ref: ArrayList<Playlist> = ArrayList()
}

/// this is global function any one can access it
/// And this function are for convert long duration to proper time
fun forMateDuration(duration: Long): String {

    val minutes = TimeUnit.MINUTES.convert(
        duration,
        TimeUnit.MILLISECONDS
    ) // duration is in milliseconds so we used milliseconds
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS)
            - minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))

    return String.format(
        "%02d:%02d",
        minutes,
        seconds
    )  // % used for value and 2d for integer value upTo 2digits
}

fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

@SuppressLint("NotifyDataSetChanged")
fun setSongPosition(increment: Boolean) {
    if (!PlayerActivity.repeat) {
        if (increment) {
            if (PlayerActivity.songPosition == PlayerActivity.musicListPA.size - 1){
                PlayerActivity.songPosition = 0
                PlayerActivity.songPlayBtn = PlayerActivity.songPosition
                MainActivity.musicAdapter.notifyDataSetChanged() // use for refresh data changes in main recyclerview
            }
            else {
                ++PlayerActivity.songPosition
                PlayerActivity.songPlayBtn = PlayerActivity.songPosition
                MainActivity.musicAdapter.notifyDataSetChanged()
            }

        } else {
            if (PlayerActivity.songPosition == 0) {
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size - 1
                PlayerActivity.songPlayBtn = PlayerActivity.songPosition
                MainActivity.musicAdapter.notifyDataSetChanged()
            }
            else {
                --PlayerActivity.songPosition
                PlayerActivity.songPlayBtn = PlayerActivity.songPosition
                MainActivity.musicAdapter.notifyDataSetChanged()
            }
        }
    }
}

fun exitApplication() {
    if (PlayerActivity.musicService != null) {
        PlayerActivity.musicService!!.apply {
            audioManager.abandonAudioFocus(PlayerActivity.musicService)
            stopForeground(true)
            mediaPlayer!!.release()
            PlayerActivity.musicService = null
        }
    }
    exitProcess(1)
}

fun favouriteChecker(id: String): Int {
    PlayerActivity.isFavourite = false
    FavouriteActivity.favListSongs.forEachIndexed { index, music ->

        if (id == music.id)
            PlayerActivity.isFavourite = true
        return index
    }
    return -1
}

fun checkPlaylist(playlist: ArrayList<Music>): ArrayList<Music> {
    playlist.forEachIndexed { index, music ->
        val file = File(music.path)
        if (!file.exists()) {
            playlist.removeAt(index)
        }
    }
    return playlist
}