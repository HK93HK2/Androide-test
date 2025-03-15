package com.example.spyapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private val REQUEST_SCREEN_RECORD = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCamera: Button = findViewById(R.id.btnCamera)
        val btnAudio: Button = findViewById(R.id.btnAudio)
        val btnScreen: Button = findViewById(R.id.btnScreen)

        btnCamera.setOnClickListener { openCamera() }
        btnAudio.setOnClickListener { toggleAudioRecording() }
        btnScreen.setOnClickListener { startScreenRecording() }
    }

    // Vérifier et demander les permissions
    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1
        )
    }

    // Ouvrir la caméra
    private fun openCamera() {
        if (!checkPermissions()) {
            requestPermissions()
            return
        }
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val videoUri: Uri? = data?.data
            Toast.makeText(this, "Vidéo enregistrée : $videoUri", Toast.LENGTH_LONG).show()
        }
        if (requestCode == REQUEST_SCREEN_RECORD && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Enregistrement d’écran activé", Toast.LENGTH_SHORT).show()
        }
    }

    // Enregistrer l’audio
    private fun toggleAudioRecording() {
        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        if (!checkPermissions()) {
            requestPermissions()
            return
        }
        val fileName = "${externalCacheDir?.absolutePath}/audio_record.3gp"
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
            start()
        }
        isRecording = true
        Toast.makeText(this, "Enregistrement audio démarré", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        mediaRecorder.apply {
            stop()
            release()
        }
        isRecording = false
        Toast.makeText(this, "Enregistrement audio terminé", Toast.LENGTH_SHORT).show()
    }

    // Enregistrement d'écran
    private fun startScreenRecording() {
        if (!checkPermissions()) {
            requestPermissions()
            return
        }
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_SCREEN_RECORD)
    }
}
