package com.example.onemusicplayer

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.onemusicplayer.databinding.FragmentNowPlayingBinding

class NowPlayingFragment : Fragment() {

    companion object {
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireContext().theme.applyStyle(MainActivity.currentTheme[MainActivity.themeIndex],true)
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        binding.playPauseBtnNP.setOnClickListener {
            if (PlayerActivity.isPlayingP) pauseMusic() else playMusic()
        }

        binding.nextBtnNP.setOnClickListener {
            setSongPosition(increment = true)
            PlayerActivity.musicService!!.createMediaPlayer()

            PlayerActivity.binding.songNamePA.text =
                PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            Glide.with(this@NowPlayingFragment)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_palyer_icon).centerCrop())
                .into(binding.songImgNP)

            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon,1F)
            playMusic()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null) {
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.isSelected = true
            Glide.with(this@NowPlayingFragment)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_palyer_icon).centerCrop())
                .into(binding.songImgNP)

            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            if (PlayerActivity.isPlayingP) binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
            else binding.playPauseBtnNP.setIconResource(R.drawable.plat_icon)
        }
        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("index", PlayerActivity.songPosition)
            intent.putExtra("class", "NowPlaying")
            ContextCompat.startActivity(requireContext(), intent, null)
        }
    }

    private fun playMusic() {
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
        PlayerActivity.binding.songPlayPausePA.setIconResource(R.drawable.pause_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon,1F)
        PlayerActivity.isPlayingP = true
    }

    private fun pauseMusic() {
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setIconResource(R.drawable.plat_icon)
        PlayerActivity.binding.songPlayPausePA.setIconResource(R.drawable.plat_icon)
        PlayerActivity.musicService!!.showNotification(R.drawable.plat_icon,0F)
        PlayerActivity.isPlayingP = false
    }
}

