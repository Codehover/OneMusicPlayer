package com.example.onemusicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {

            ApplicationClass.PREVIOUS -> setPreNextSong(increment = false, context = context!!)

            ApplicationClass.PLAY -> {
                if (PlayerActivity.isPlayingP) pauseMusic() else playMusic()
            }
            ApplicationClass.NEXT -> setPreNextSong(increment = true, context = context!!)
            ApplicationClass.EXIT -> {
               exitApplication() // this function is created globally in music.kt
            }
        }
    }

    private fun playMusic() {
        PlayerActivity.isPlayingP = true
        PlayerActivity.musicService?.mediaPlayer?.start()
        PlayerActivity.musicService?.showNotification(R.drawable.pause_icon,1F)
        PlayerActivity.binding.songPlayPausePA.setIconResource(R.drawable.pause_icon)
        NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
    }

    private fun pauseMusic() {
        PlayerActivity.isPlayingP = false
        PlayerActivity.musicService?.mediaPlayer?.pause()
        PlayerActivity.musicService?.showNotification(R.drawable.plat_icon,0F)
        PlayerActivity.binding.songPlayPausePA.setIconResource(R.drawable.plat_icon)
        NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.plat_icon)
    }

    private fun setPreNextSong(increment: Boolean, context: Context) {

        setSongPosition(increment = increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_palyer_icon).centerCrop())
            .into(PlayerActivity.binding.songImagePA)

        PlayerActivity.binding.songNamePA.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_palyer_icon).centerCrop())
            .into(NowPlayingFragment.binding.songImgNP)

        NowPlayingFragment.binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playMusic()
        PlayerActivity.fIndex = favouriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if (PlayerActivity.isFavourite) PlayerActivity.binding.favouriteButtonPA.setImageResource(R.drawable.favourite)
        else PlayerActivity.binding.favouriteButtonPA.setImageResource(R.drawable.fav_empty_icon)

    }
}
