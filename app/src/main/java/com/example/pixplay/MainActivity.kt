package com.example.pixplay

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.pixplay.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    companion object{
        lateinit var videoList: ArrayList<Video>
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //for Nav Drawer
        toggle=ActionBarDrawerToggle(this,binding.root,R.string.open,R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        if (requestRuntimePermission()){
            videoList = getAllVideos()
            setFragment(VideoFragment())
        }
        binding.bottomNav.setOnItemSelectedListener{
            when(it.itemId){
                R.id.videoView -> setFragment(VideoFragment())
                R.id.folderView -> setFragment(FoldersFragment())
            }
            return@setOnItemSelectedListener true
        }
    }
    private fun setFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentFL,fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }
    // for requesting permission
    private fun requestRuntimePermission(): Boolean{
        if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) !=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 13)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            else
                ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 13)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }
    @SuppressLint("Recycle", "Range", "SuspiciousIndentation")
    private fun getAllVideos(): ArrayList<Video>{
        val tempList = ArrayList<Video>()
        val projection = arrayOf(MediaStore.Video.Media.TITLE,MediaStore.Video.Media.SIZE,MediaStore.Video.Media._ID,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,MediaStore.Video.Media.DATA,MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION)
        val cursor = this.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,null,null,
        MediaStore.Video.Media.DATE_ADDED + "DESC")
        if (cursor!=null)
            if (cursor.moveToNext())
                do{
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val sizeC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val durationC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)).toLong()

                    try {
                        val file = File(pathC)
                        val artUric = Uri.fromFile(file)
                        val video = Video(title = titleC, id = idC, folderName = folderC, duration = durationC, size =sizeC,
                        path = pathC, artUi = artUric)
                        if (file.exists())tempList.add(video)
                    }catch (e:Exception){}
                }while (cursor.moveToNext())
                cursor?.close()
        return tempList
    }
}