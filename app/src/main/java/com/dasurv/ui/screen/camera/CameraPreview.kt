package com.dasurv.ui.screen.camera

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dasurv.util.imageProxyToBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
internal fun CameraPreview(
    onImageDimensionsChanged: (width: Int, height: Int) -> Unit,
    onFaceDetected: (Face, Bitmap) -> Unit,
    onRotationDetected: (rotation: Int, isFrontCamera: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val faceDetector = androidx.compose.runtime.remember {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setMinFaceSize(0.3f)
            .build()
        FaceDetection.getClient(options)
    }
    val executor = androidx.compose.runtime.remember { Executors.newSingleThreadExecutor() as ExecutorService }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            faceDetector.close()
            executor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(android.util.Size(480, 640))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    // ML Kit returns face coordinates in rotated space,
                    // so swap width/height when rotation is 90 or 270
                    val rotation = imageProxy.imageInfo.rotationDegrees
                    if (rotation == 90 || rotation == 270) {
                        onImageDimensionsChanged(imageProxy.height, imageProxy.width)
                    } else {
                        onImageDimensionsChanged(imageProxy.width, imageProxy.height)
                    }

                    // Store rotation for bitmap correction when saving
                    onRotationDetected(rotation, true)

                    @androidx.annotation.OptIn(ExperimentalGetImage::class)
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage, rotation
                        )
                        faceDetector.process(inputImage)
                            .addOnSuccessListener { faces ->
                                if (faces.isNotEmpty()) {
                                    val face = faces[0]
                                    val bitmap = imageProxyToBitmap(imageProxy)
                                    if (bitmap != null) {
                                        onFaceDetected(face, bitmap)
                                    }
                                }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}
