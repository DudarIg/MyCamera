package ru.dudar.mycamera

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Size
import android.widget.ImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import java.util.jar.Manifest

private const val REQUEST = 1212

class MainActivity : AppCompatActivity() {

    lateinit var preview : ImageView
    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    val transl = YUVtoRGB()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preview = findViewById(R.id.preview_image_view)

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), REQUEST)
        } else {
            initCamera()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST &&
            grantResults.size > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initCamera()

        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun initCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()

            // выбираем какую камеру используем
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            //val previewImage = Preview.Builder().build()

            //val imageCapture = ImageCapture.Builder().build()

            // выбираем сценарий работы с камерой
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(768, 1024))
                .setTargetRotation(this.windowManager.defaultDisplay.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this),
                ImageAnalysis.Analyzer {
                    val img = it.image
                    val bitmap = transl.translateYUV(img, this)
                    //preview.rotation
                    preview.setImageBitmap(bitmap.rotate(90f))
                    it.close()
                }
                )
            cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)

        }, ContextCompat.getMainExecutor(this))
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }


}

