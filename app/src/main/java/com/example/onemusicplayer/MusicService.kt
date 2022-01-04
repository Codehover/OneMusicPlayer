package com.example.onemusicplayer


import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC


class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {

    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat  // unique media session for our application
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(p0: Intent?): IBinder? {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }


    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun showNotification(playPauseBtn: Int, playbackSpeed: Float) {

        // this code for handling click on notification
        val intent = Intent(baseContext, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(baseContext, 0, intent, 0)

        //// intent for broadcast this all intent passes in notification builder with content addAction()
        val preIntent = Intent(
            baseContext,
            NotificationReceiver::class.java
        ).setAction(ApplicationClass.PREVIOUS)
        val prePendingIntent =
            PendingIntent.getBroadcast(baseContext, 0, preIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.NEXT)

        val nextPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val exitIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            exitIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        ///// all notification intent finished

        val imgArt = getImgArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image = if (imgArt != null) {
            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
        } else {
            BitmapFactory.decodeResource(
                resources,
                R.drawable.ic_launcher_background
            )
        }

        val builder = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setSmallIcon(R.drawable.music_icon)
            .setContentIntent(contentIntent)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setLargeIcon(image)
            .setOnlyAlertOnce(true)
            .setVisibility(VISIBILITY_PUBLIC)
            .addAction(R.drawable.back_aaa, "Previous", prePendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.next_aaa, "Next", nextPendingIntent)
            .addAction(R.drawable.exit_icon, "Exit", exitPendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        mediaPlayer!!.duration.toLong()
                    )
                    .build()
            )
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        mediaPlayer!!.currentPosition.toLong(),
                        playbackSpeed
                    )
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build()
            )
        }
        startForeground(13, builder)
    }

    fun createMediaPlayer() {
        try {
            if (PlayerActivity.musicService?.mediaPlayer == null) {
                PlayerActivity.musicService?.mediaPlayer = MediaPlayer()
            }
            PlayerActivity.musicService?.mediaPlayer?.apply {
                reset()
                setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
                prepare()
                PlayerActivity.binding.songPlayPausePA.setIconResource(R.drawable.pause_icon)
                PlayerActivity.musicService?.showNotification(R.drawable.pause_icon, 1F)

                PlayerActivity.binding.tvSeekBarStart.text =
                    forMateDuration(mediaPlayer!!.currentPosition.toLong())
                PlayerActivity.binding.tvSeekBarEnd.text =
                    forMateDuration(mediaPlayer!!.duration.toLong())
                PlayerActivity.binding.songSeekBarPA.progress = 0
                PlayerActivity.binding.songSeekBarPA.max =
                    PlayerActivity.musicService!!.mediaPlayer!!.duration
                PlayerActivity.nowPlayingId =
                    PlayerActivity.musicListPA[PlayerActivity.songPosition].id
            }
        } catch (ex: Exception) {
            return
        }
    }

    fun setupSeekBar() {
        runnable = Runnable {
            PlayerActivity.binding.tvSeekBarStart.text =
                forMateDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.songSeekBarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange >= 0) {
            // pause music
            PlayerActivity.binding.songPlayPausePA.setIconResource(R.drawable.plat_icon)
            NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.plat_icon)
            showNotification(R.drawable.plat_icon, 0F)
            PlayerActivity.isPlayingP = false
            mediaPlayer?.pause()
        } else {
            // play music
            PlayerActivity.binding.songPlayPausePA.setIconResource(R.drawable.pause_icon)
            NowPlayingFragment.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
            showNotification(R.drawable.pause_icon, 1F)
            PlayerActivity.isPlayingP = true
            mediaPlayer?.start()

        }
    }
}