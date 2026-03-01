package com.dasurv.ui.screen.tryon

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.model.Pigment
import com.dasurv.ui.screen.camera.LipOverlay
import com.dasurv.ui.theme.GlassBrush
import com.dasurv.ui.util.parseHexSafe
import com.dasurv.util.imageProxyToBitmap
import com.dasurv.util.loadBitmapFromUri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientTryOnScreen(
    clientId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ClientTryOnViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val client by viewModel.client.collectAsStateWithLifecycle()
    val selectedBrand by viewModel.selectedBrand.collectAsStateWithLifecycle()
    val pigments by viewModel.pigments.collectAsStateWithLifecycle()
    val selectedPigment by viewModel.selectedPigment.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsStateWithLifecycle()
    val detectedFace by viewModel.detectedFace.collectAsStateWithLifecycle()
    val staticBitmap by viewModel.staticBitmap.collectAsStateWithLifecycle()
    val imageWidth by viewModel.imageWidth.collectAsStateWithLifecycle()
    val imageHeight by viewModel.imageHeight.collectAsStateWithLifecycle()
    val dualAnalysis by viewModel.dualAnalysis.collectAsStateWithLifecycle()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                viewModel.analyzeStaticPhoto(bitmap)
            }
        }
    }

    LaunchedEffect(clientId) {
        viewModel.loadClient(clientId)
    }

    // Track loading state
    LaunchedEffect(pigments) {
        if (pigments.isNotEmpty()) isLoading = false
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) hasCameraPermission = true
        else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Top bar ---
        Surface(
            color = Color.Black,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    "Try-On Preview",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                // Mode toggle with animated icon swap
                IconButton(onClick = {
                    if (mode == TryOnMode.LIVE_CAMERA) {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    } else {
                        viewModel.setMode(TryOnMode.LIVE_CAMERA)
                    }
                }) {
                    AnimatedContent(
                        targetState = mode,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                        label = "mode_icon"
                    ) { currentMode ->
                        Icon(
                            if (currentMode == TryOnMode.LIVE_CAMERA) Icons.Default.PhotoLibrary
                            else Icons.Default.CameraAlt,
                            contentDescription = if (currentMode == TryOnMode.LIVE_CAMERA) "Open gallery" else "Switch to camera",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // --- Camera / Photo preview area ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)
                .background(Color.Black)
        ) {
            if (mode == TryOnMode.STATIC_PHOTO && staticBitmap != null) {
                // Blurred background layer (vivi-music style)
                Image(
                    bitmap = staticBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(80.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
                // Sharp foreground
                Image(
                    bitmap = staticBitmap!!.asImageBitmap(),
                    contentDescription = "Static photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (hasCameraPermission) {
                val tryOnFaceDetector = remember {
                    val options = FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setMinFaceSize(0.3f)
                        .build()
                    FaceDetection.getClient(options)
                }
                val tryOnExecutor = remember { Executors.newSingleThreadExecutor() as ExecutorService }

                DisposableEffect(Unit) {
                    onDispose {
                        tryOnFaceDetector.close()
                        tryOnExecutor.shutdown()
                    }
                }

                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        val executor = tryOnExecutor
                        val faceDetector = tryOnFaceDetector

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = androidx.camera.core.Preview.Builder().build().also {
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
                                    viewModel.updateImageDimensions(imageProxy.height, imageProxy.width)
                                } else {
                                    viewModel.updateImageDimensions(imageProxy.width, imageProxy.height)
                                }

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
                                                    viewModel.onFaceDetected(face, bitmap)
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
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Camera permission required",
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Lip overlay with blended color preview
            val pigmentHex = selectedPigment?.colorHex
            LipOverlay(
                face = detectedFace,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                isFrontCamera = mode == TryOnMode.LIVE_CAMERA,
                arMode = pigmentHex != null,
                upperLipPigmentHex = pigmentHex,
                lowerLipPigmentHex = pigmentHex,
                naturalUpperLipHex = dualAnalysis?.upperLip?.dominantColorHex,
                naturalLowerLipHex = dualAnalysis?.lowerLip?.dominantColorHex
            )

            // No face message with animated visibility
            androidx.compose.animation.AnimatedVisibility(
                visible = detectedFace == null && (mode == TryOnMode.STATIC_PHOTO || hasCameraPermission),
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Text(
                        if (mode == TryOnMode.STATIC_PHOTO) "No face detected in photo"
                        else "Position your face in the camera",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // Selected pigment info overlay with AnimatedContent
            AnimatedContent(
                targetState = selectedPigment,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                label = "selected_pigment_overlay"
            ) { pigment ->
                if (pigment != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .background(GlassBrush, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(parseHexSafe(pigment.colorHex))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            )
                            Column {
                                Text(
                                    pigment.name,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .widthIn(max = 200.dp)
                                        .basicMarquee(iterations = 1, initialDelayMillis = 2000)
                                )
                                Text(
                                    pigment.brand.displayName,
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Filters area ---
        PigmentFilterPanel(
            selectedBrand = selectedBrand,
            onSelectBrand = { viewModel.selectBrand(it) },
            showFavoritesOnly = showFavoritesOnly,
            onToggleFavorites = { viewModel.toggleShowFavoritesOnly() },
            isLoading = isLoading,
            pigments = pigments,
            selectedPigment = selectedPigment,
            isFavorite = { viewModel.isFavorite(it) },
            onSelectPigment = { viewModel.selectPigment(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}