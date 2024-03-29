package com.example.myapplication

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

const val CAMERA_REQUEST_CODE = 1
const val CAMERA_PERMISSION_REQUEST_CODE = 2

class MainActivity : AppCompatActivity() {

    private lateinit var file: File
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Camerabutton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23) checkPermission() else cameraIntent()
        }
    }

    private fun requestPermission() {
        val str: Array<String> = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            ActivityCompat.requestPermissions(this, str, CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) cameraIntent() else requestPermission()
    }

    private fun cameraIntent() {
        val folder = getExternalFilesDir(Environment.DIRECTORY_DCIM)
        val name = SimpleDateFormat("ddHHmmss", Locale.US).format(Date()).let {
            String.format("CameraIntent_%s.jpeg",it)
        }

        file = File(folder, name)
        uri = FileProvider.getUriForFile(
            this.applicationContext, "$packageName.fileprovider",
            file
        )

        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, uri).run {
                startActivityForResult(this, CAMERA_REQUEST_CODE)
            }
    }

    private fun registerDatabase(file: File) {
        val contentValues = ContentValues().also {
            it.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
            it.put("_data", file.absolutePath)
        }
        this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                cameraIntent() else Toast.makeText(
                this,
                "これ以上なにもないよ",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE) {
            registerDatabase(file)
            val data = data?.extras?.get("data") as Bitmap
            HttpResonsAsync(data)
            Intent(this, Image::class.java).run {
                this.putExtra("uri", uri)
                startActivity(this)
            }
        }
    }

    private fun postMultipart(image: Image,string: String){

    }
}
