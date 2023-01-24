package com.kej.recodedemoapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.kej.recodedemoapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_RECORD_AUDIO_CODE = 200
    }

    private lateinit var binding: ActivityMainBinding
    private var record: MediaRecorder? = null
    private var fileName: String? = null
    private var player: MediaPlayer? = null
    private var state = RecorderState.Release

    enum class RecorderState {
        Release,
        Recording,
        Playing
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        fileName = "${externalCacheDir?.absolutePath}/audiotest.3gp"
        initViews()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val audioRecordPermissionGranted = (requestCode == REQUEST_RECORD_AUDIO_CODE)
                && (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED)

        if (audioRecordPermissionGranted) {
            initRecord(true)
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                showPermissionRationalDialog()
            } else {
                showPermissionSettingDialog()
            }
        }
    }

    private fun initViews() {
        binding.recordButton.setOnClickListener {
            when (state) {
                RecorderState.Release -> {
                    checktAudioPermission()
                }
                RecorderState.Recording -> {
                    initRecord(false)
                }
                RecorderState.Playing -> {

                }
            }
        }

        binding.playButton.setOnClickListener {
            when (state) {
                RecorderState.Release -> {
                    onPlay(true)
                }
                else ->{}
            }
        }

        binding.playButton.setOnClickListener {
            when (state) {
                RecorderState.Playing -> {
                    onPlay(false)
                }
                else -> {}
            }
        }
    }


    private fun checktAudioPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) -> {
                showPermissionRationalDialog()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE
                )
            }
        }
    }

    private fun initRecord(isPlayable: Boolean) {
        if (isPlayable) {
            startRecord()
        } else {
            stopRecord()
        }
    }

    private fun startRecord() {
        record = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
            } catch (e: Exception) {
                Log.e("MediaRecorder", "PlayerFailed: $e")

            }
            start()
        }
        state = RecorderState.Playing

        with(binding) {
            recordButton.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_baseline_stop_24))
            recordButton.imageTintList = ColorStateList.valueOf(Color.BLACK)
            playButton.isEnabled = false
            playButton.alpha = 0.3f
        }
    }

    private fun stopRecord() {
        record?.apply {
            stop()
            release()
        }
        record = null
        state = RecorderState.Release

        with(binding) {
            recordButton.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_baseline_fiber_manual_record_24))
            recordButton.imageTintList = ColorStateList.valueOf(Color.RED)
            binding.playButton.isEnabled = true
            playButton.alpha = 1.0f

        }

    }

    private fun onPlay(start: Boolean) {
        if (start) {
            startPlaying()
        } else {
            stopPlaying()
        }
    }


    private fun startPlaying() {
        state = RecorderState.Playing
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
            } catch (e: Exception) {
                Log.e("MediaPlayer","Exception: $e")
            }
            start()
        }
        player?.setOnCompletionListener {
            stopPlaying()
        }

        with(binding) {
            recordButton.isEnabled = false
            recordButton.alpha = 0.3f
        }
    }

    private fun stopPlaying() {
        state = RecorderState.Release
        player?.release()
        player = null

        binding.recordButton.apply {
            isEnabled = true
            alpha = 1.0f

        }
    }

    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("앱 권한을 허용해주셔야 앱을 사용할 수 있습니다.")
            setPositiveButton("권한 허용하기") { _, _ ->
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE
                )
            }
            setNegativeButton("취소", null)
        }
    }

    private fun showPermissionSettingDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("녹음 권한을 허용해야 녹음을 실행할 수 있습니다.")
            setPositiveButton("권한 허용하기") { _, _ ->
                navigateAppSetting()
                setNegativeButton("취소", null)
            }
        }
    }

    private fun navigateAppSetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }.let(::startActivity)
        } else {
            Intent(Settings.ACTION_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                startActivityForResult(this, 0)
            }
        }
    }

}


