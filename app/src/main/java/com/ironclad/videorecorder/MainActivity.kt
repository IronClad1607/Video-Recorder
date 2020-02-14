package com.ironclad.videorecorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.TextureView
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File


private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS =
    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
private val tag = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var viewFinder: TextureView
    private lateinit var captureButton: ImageButton
    private lateinit var videoCapture: VideoCapture

    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewFinder = findViewById(R.id.view_finder)
        captureButton = findViewById(R.id.capture_button)

        if (allPermissionGranted()) {
            viewFinder.post {
                startCamera()
            }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.mp4")

        captureButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                captureButton.setBackgroundColor(Color.GREEN)
                videoCapture.startRecording(file, object : VideoCapture.OnVideoSavedListener {
                    override fun onVideoSaved(file: File?) {
                        Log.d("PUI", "Video File: $file")
                    }

                    override fun onError(
                        useCaseError: VideoCapture.UseCaseError?,
                        message: String?,
                        cause: Throwable?
                    ) {
                        Log.d("PUI", "Video Error : $message")
                    }

                })
            } else if (event.action == MotionEvent.ACTION_UP) {
                captureButton.setBackgroundColor(Color.RED)
                videoCapture.stopRecording()
                Log.d("PUI", "Video File Stopped")
            }
            false
        }


    }

    private fun allPermissionGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionGranted()) {
                viewFinder.post {
                    startCamera()
                }
            } else {
                Toast.makeText(this, "Permissions not granted by user.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        val previewConfig = PreviewConfig.Builder().build()

        val preview = Preview(previewConfig)

        val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
            setTargetRotation(viewFinder.display.rotation)
        }.build()

        videoCapture = VideoCapture(videoCaptureConfig)


        preview.setOnPreviewOutputUpdateListener {
            viewFinder.surfaceTexture = it.surfaceTexture
        }

        CameraX.bindToLifecycle(this, preview, videoCapture)
    }

}
