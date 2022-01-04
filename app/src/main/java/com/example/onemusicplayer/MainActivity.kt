package com.example.onemusicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onemusicplayer.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    companion object {
        lateinit var MusicListMA: ArrayList<Music>
        lateinit var musicListSearch: ArrayList<Music>
        @SuppressLint("StaticFieldLeak")
        lateinit var musicAdapter: MusicAdapter
        var search: Boolean = false
        var themeIndex: Int = 0
        val currentTheme = arrayOf(
            R.style.coolPink,
            R.style.coolBlue,
            R.style.coolPurple,
            R.style.coolGreen,
            R.style.coolBlack
        )
        val currentThemeNav = arrayOf(
            R.style.coolPinkNav,
            R.style.coolBlueNav,
            R.style.coolPurpleNav,
            R.style.coolGreenNav,
            R.style.coolBlackNav
        )
        val currentGradient = arrayOf(
            R.drawable.gradient_pink,
            R.drawable.gradient_blue,
            R.drawable.gradient_purple,
            R.drawable.gradient_green,
            R.drawable.gradient_black
        )
        var sortOrder: Int = 0
        val sortingList = arrayOf(
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeEditor = getSharedPreferences("THEMES", MODE_PRIVATE)
        themeIndex = themeEditor.getInt("themeIndex", 0)
        setTheme(currentThemeNav[themeIndex])
        //// binding layout to activity
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //for nav drawer
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle) // this will attach toggle to layout
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (requestRuntimePermission()) {
            initializeCode()

            // for retrieve favourites data using shared Preferences
            FavouriteActivity.favListSongs = ArrayList()
            val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
            val jsonString = editor.getString("favouriteSongs", null)
            val typeToken = object : TypeToken<ArrayList<Music>>() {}.type
            if (jsonString != null) {
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
                FavouriteActivity.favListSongs.addAll(data)
            }

            // for retrieve playlist data using shared Preferences
            PlaylistActivity.musicPlaylist = MusicPlaylist()
            val jsonStringPlaylist = editor.getString("musicPlaylist", null)
            if (jsonStringPlaylist != null) {
                val dataPlaylist: MusicPlaylist =
                    GsonBuilder().create().fromJson(jsonStringPlaylist, MusicPlaylist::class.java)
                PlaylistActivity.musicPlaylist = dataPlaylist
            }
        }
        binding.shuffle.setOnClickListener {
            val playerIntent = Intent(this@MainActivity, PlayerActivity::class.java)
            playerIntent.putExtra("index", 0)
            playerIntent.putExtra("class", "MainActivity")
            startActivity(playerIntent)
        }

        binding.favourite.setOnClickListener {
            val favIntent = Intent(this@MainActivity, FavouriteActivity::class.java)
            startActivity(favIntent)
        }

        binding.playlist.setOnClickListener {
            val playListIntent = Intent(this@MainActivity, PlaylistActivity::class.java)
            startActivity(playListIntent)
        }

        binding.navVieww.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navFeedBack -> startActivity(
                    Intent(
                        this@MainActivity,
                        FeedbackActivity::class.java
                    )
                )
                R.id.navAbout -> startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                R.id.navSettings -> startActivity(
                    Intent(
                        this@MainActivity,
                        SetttingsActivity::class.java
                    )
                )
                R.id.navExit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit")
                        .setMessage("Do you want to close app?")
                        .setPositiveButton("Yes") { _, _ ->
                            exitApplication()
                        }
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                    val customDialog = builder.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
                }
            }
            true
        }
    }

    ////for requesting permission to user
    private fun requestRuntimePermission(): Boolean {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                111
            )
            return false
        } else {
            return true
        }
    }

    /// result of user given permission at runtime
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                initializeCode()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    111
                )
            }
        }
    }

    // this function is mandatory to show drawer layout
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)

    }

    /// code for initializeCode
    @SuppressLint("SetTextI18n")
    private fun initializeCode() {

        search = false
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        sortOrder = sortEditor.getInt("sortOrder", 0)
        MusicListMA = getAllAudio()
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)

        binding.mainPlayerRec.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = musicAdapter
        }
        binding.totalSong.text = "Total songs - ${musicAdapter.itemCount}"
    }

    @SuppressLint("Recycle", "Range")
    private fun getAllAudio(): ArrayList<Music> {

        val tempList = ArrayList<Music>()
        val selection =
            MediaStore.Audio.Media.IS_MUSIC + "!=0"  // will info cursor which type of file we want
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortingList[sortOrder],
            null
        )

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

                    val albumIdC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                            .toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUri = Uri.withAppendedPath(uri, albumIdC).toString()

                    val music = Music(
                        id = idC,
                        title = titleC,
                        album = albumC,
                        artist = artistC,
                        duration = durationC,
                        path = pathC,
                        artUri = artUri
                    )

                    val file = File(music.path)
                    if (file.exists()) {
                        tempList.add(music)
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return tempList
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)
        // for setting gradients
        findViewById<LinearLayout>(R.id.linearLayout_nav)?.setBackgroundResource(currentGradient[themeIndex])

        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean =
                true // this method will call when user will press enter button

            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = ArrayList()
                if (newText != null) {
                    val userInput = newText.lowercase()
                    for (song in MusicListMA) {
                        if (song.title.lowercase().contains(userInput)) {
                            musicListSearch.add(song)
                        }
                    }
                    search = true
                    musicAdapter.updatedMusicList(searchList = musicListSearch)
                }

                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if(PlayerActivity.songPlayBtnIcon) musicAdapter.notifyDataSetChanged()

        // for storing favourites data using shared Preferences
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavouriteActivity.favListSongs)
        editor.putString("favouriteSongs", jsonString)

        /// work for storing playList data
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("musicPlaylist", jsonStringPlaylist)
        editor.apply()

        // work for sorting
        val sortEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortValue = sortEditor.getInt("sortOrder", 0)
        if (sortOrder != sortValue) {
            sortOrder = sortValue
            MusicListMA = getAllAudio()
            musicAdapter.updatedMusicList(MusicListMA)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlayingP && PlayerActivity.musicService != null) exitApplication()
    }
}