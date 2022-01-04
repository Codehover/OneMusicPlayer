package com.example.onemusicplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.onemusicplayer.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
        var isPlayingP: Boolean = false
        var musicService: MusicService? = null
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayingId: String = ""
        var isFavourite: Boolean = false
        var fIndex: Int = -1
        var fromIntent: Boolean = false
        var songPlayBtn: Int = -1
        var songPlayBtnIcon: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.data?.scheme.contentEquals("content")) {
            fromIntent = true
            val sIntent = Intent(this@PlayerActivity, MusicService::class.java)
            bindService(sIntent, this, BIND_AUTO_CREATE)
            startService(sIntent)
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))
            Glide.with(this@PlayerActivity)
                .load(getImgArt(musicListPA[songPosition].path))
                .apply(RequestOptions().placeholder(R.drawable.music_palyer_icon).centerCrop())
                .into(binding.songImagePA)
            binding.songNamePA.text = musicListPA[songPosition].title
        } else initializeLayout()

        binding.backButtonPA.setOnClickListener { finish() }
        binding.songPlayPausePA.setOnClickListener {
            if (isPlayingP) pauseMusic() else playMusic()
        }
        binding.songBackPA.setOnClickListener {
            preNexSong(increment = false)
        }
        binding.songNextPA.setOnClickListener {
            preNexSong(increment = true)
        }
        binding.songSeekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })
        binding.repeatBtnPA.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.repeatBtnPA.setColorFilter(
                    ContextCompat.getColor(
                        this@PlayerActivity,
                        R.color.purple_500
                    )
                )
            } else {
                repeat = false
                binding.repeatBtnPA.setColorFilter(
                    ContextCompat.getColor(
                        this@PlayerActivity,
                        R.color.cool_pink
                    )
                )
            }
        }
        binding.equalizerBtnPA.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(
                    AudioEffect.EXTRA_AUDIO_SESSION,
                    musicService!!.mediaPlayer!!.audioSessionId
                )
                eqIntent.putExtra(
                    AudioEffect.EXTRA_PACKAGE_NAME,
                    baseContext.packageName
                ) // package name is mandatory other wise system will controll entire phone sound in system
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)
            } catch (e: Exception) {
                Toast.makeText(this, "Equalizer Feature is not supported!!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        binding.timerBtnPA.setOnClickListener {
            val timer = min15 || min30 || min60
            if (!timer) showBottomSheetDialog()
            else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop timer?")
                    .setPositiveButton("Yes") { _, _ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.timerBtnPA.setColorFilter(
                            ContextCompat.getColor(
                                this,
                                R.color.cool_pink
                            )
                        )
                    }
                    .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
        }
        binding.shareBtnPA.setOnClickListener {
            val sharedIntent = Intent()
            sharedIntent.action = Intent.ACTION_SEND
            sharedIntent.type = "audio/*"
            sharedIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(sharedIntent, "Sharing Music File!"))
        }
        binding.favouriteButtonPA.setOnClickListener {
            if (isFavourite) {
                isFavourite = false
                binding.favouriteButtonPA.setImageResource(R.drawable.fav_empty_icon)
                FavouriteActivity.favListSongs.removeAt(fIndex)

            } else {
                isFavourite = true
                binding.favouriteButtonPA.setImageResource(R.drawable.favourite)
                FavouriteActivity.favListSongs.add(musicListPA[songPosition])

            }
        }
    }

    // Important function
    private fun initializeLayout() {
        songPosition = intent!!.getIntExtra("index", 0)
        when (intent!!.getStringExtra("class")) {
            "MusicAdapter" -> {
                serviceIntentCode()
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }
            "MainActivity" -> {
                serviceIntentCode()
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
            }
            "MusicAdapterSearch" -> {
                serviceIntentCode()
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }
            "NowPlaying" -> {
                setLayout()
                binding.tvSeekBarStart.text =
                    forMateDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text =
                    forMateDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.songSeekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.songSeekBarPA.max = musicService!!.mediaPlayer!!.duration
                if (isPlayingP) {
                    binding.songPlayPausePA.setIconResource(R.drawable.pause_icon)
                } else {
                    binding.songPlayPausePA.setIconResource(R.drawable.plat_icon)
                }
            }
            "FavouriteAdapter" -> {
                serviceIntentCode()
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favListSongs)
                setLayout()

            }
            "FavouriteShuffle" -> {
                serviceIntentCode()
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favListSongs)
                musicListPA.shuffle()
                setLayout()
            }
            "PlayListDetailAdapter" -> {
                serviceIntentCode()
                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetail.currentPlaylistPosition].playlist)
                setLayout()
            }
            "PlaylistDetailShuffle" -> {
                serviceIntentCode()
                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favListSongs)
                musicListPA.shuffle()
                setLayout()
            }
        }
    }

    private fun setLayout() {
        fIndex = favouriteChecker(musicListPA[songPosition].id)

        Glide.with(this@PlayerActivity)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_palyer_icon).centerCrop())
            .into(binding.songImagePA)

        binding.songNamePA.text = musicListPA[songPosition].title
        if (repeat) binding.repeatBtnPA.setColorFilter(
            ContextCompat.getColor(
                this@PlayerActivity,
                R.color.purple_500
            )
        )
        if (min15 || min30 || min60) binding.timerBtnPA.setColorFilter(
            ContextCompat.getColor(
                this,
                R.color.purple_500
            )
        )
        if (isFavourite) binding.favouriteButtonPA.setImageResource(R.drawable.favourite)
        else binding.favouriteButtonPA.setImageResource(R.drawable.fav_empty_icon)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun createMediaPlayer() {
        try {
            if (musicService?.mediaPlayer == null){ musicService?.mediaPlayer = MediaPlayer() }

            musicService?.mediaPlayer?.apply {
                reset()
                setDataSource(musicListPA[songPosition].path)
                prepare()
                start()
                isPlayingP = true
                songPlayBtn = songPosition
                if (!songPlayBtnIcon) songPlayBtnIcon = true

                binding.songPlayPausePA.setIconResource(R.drawable.pause_icon)
                musicService!!.showNotification(R.drawable.pause_icon, 1F)
                binding.tvSeekBarStart.text =
                    forMateDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text =
                    forMateDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.songSeekBarPA.progress = 0
                binding.songSeekBarPA.max = musicService!!.mediaPlayer!!.duration
                musicService!!.mediaPlayer!!.setOnCompletionListener(this@PlayerActivity)
                nowPlayingId = musicListPA[songPosition].id
            }
        } catch (ex: Exception) {
            return
        }
    }

    private fun playMusic() {
        binding.songPlayPausePA.setIconResource(R.drawable.pause_icon)
        musicService?.showNotification(R.drawable.pause_icon, 1F)
        isPlayingP = true
        musicService?.mediaPlayer?.start()
    }

    private fun pauseMusic() {
        binding.songPlayPausePA.setIconResource(R.drawable.plat_icon)
        musicService?.showNotification(R.drawable.plat_icon, 0F)
        isPlayingP = false
        musicService?.mediaPlayer?.pause()
    }

    private fun preNexSong(increment: Boolean) {
        if (increment) {
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        } else {
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }


    ///service work code
    override fun onServiceConnected(componentName: ComponentName?, servicee: IBinder?) {
        val binder = servicee as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.setupSeekBar()
        musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audioManager.requestAudioFocus(
            musicService,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }

    // this will call on songCompletion
    override fun onCompletion(p0: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout()
        } catch (e: java.lang.Exception) {
            return
        }
    }
    // catch activity result here
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 || resultCode == RESULT_OK) {
            return
        }
    }
    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 15 minutes", Toast.LENGTH_SHORT)
                .show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min15 = true
            Thread {
                Thread.sleep((60000 * 15).toLong())
                if (min15) exitApplication()
            }.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 30 minutes", Toast.LENGTH_SHORT)
                .show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min30 = true
            Thread {
                Thread.sleep((60000 * 30).toLong())
                if (min30) exitApplication()
            }.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 60 minutes", Toast.LENGTH_SHORT)
                .show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min60 = true
            Thread {
                Thread.sleep((60000 * 60).toLong())
                if (min60) exitApplication()
            }.start()
            dialog.dismiss()
        }
    }
    //// this code is for service bind and start
    private fun serviceIntentCode() {
        // for start service code
        val intent = Intent(this@PlayerActivity, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
    }

    private fun getMusicDetails(contentUri: Uri): Music {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val duration = durationColumn?.let { cursor.getLong(it)}!!

            return Music(
                id = "Unknown",
                title = path.toString(),
                album = "Unknown",
                duration = duration,
                artUri = "Unknown",
                path = path.toString(),
                artist = "Unknown"
            )
        } finally {
            cursor?.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (musicListPA[songPosition].id == "Unknown" && !isPlayingP) {
            exitApplication()
        }
    }
}