package com.example.laporbang.presentation.view.detection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.laporbang.presentation.view.auth.COLORS_BG
import com.example.laporbang.presentation.view.auth.COLORS_INPUT_BORDER
import com.example.laporbang.presentation.view.auth.COLORS_PRIMARY
import com.example.laporbang.presentation.view.auth.COLORS_SURFACE
import com.example.laporbang.presentation.view.auth.COLORS_TEXT
import com.example.laporbang.presentation.view.auth.COLORS_TEXT_SECONDARY
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onBackClick: () -> Unit = {},
//    onCapturePhoto: () -> Unit = {},
    onCapturePhoto: (Double, Double) -> Unit = { _, _ -> },
    onLocationClick: () -> Unit = {},
    initialLocation: String? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }

    var currentLocation by remember { mutableStateOf("Mencari lokasi...") }
    var currentLat by remember { mutableDoubleStateOf(0.0) }
    var currentLng by remember { mutableDoubleStateOf(0.0) }

//    var currentLocation by remember { mutableStateOf("Mencari lokasi...") }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    LaunchedEffect(initialLocation) {
        if (initialLocation != null) {
            currentLocation = initialLocation
        }
    }

    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {

            Log.d("CameraScreen", "Foto dari galeri: $uri")
        }
    }

    LaunchedEffect(lensFacing, permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        } else {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted && initialLocation == null) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentLat = location.latitude
                        currentLng = location.longitude

                        scope.launch(Dispatchers.IO) {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            try {
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    val address = addresses[0]
                                    val street = address.thoroughfare ?: address.featureName ?: ""
                                    val city = address.subAdminArea ?: address.adminArea ?: ""
                                    withContext(Dispatchers.Main) {
                                        currentLocation = "$street, $city".trim(',', ' ')
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) { currentLocation = "Lokasi tidak dikenali" }
                            }
                        }
                    } else {
                        currentLocation = "Menunggu sinyal GPS..."
                    }
                }
            } catch (e: SecurityException) {
                currentLocation = "Izin lokasi ditolak"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (permissionsState.allPermissionsGranted) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(COLORS_BG),
                contentAlignment = Alignment.Center
            ) {
                Text("Mohon izinkan akses kamera & lokasi", color = Color.White)
            }
        }

        CameraTopBar(
            location = currentLocation,
            isFlashOn = flashMode == ImageCapture.FLASH_MODE_ON,
            onBackClick = onBackClick,
            onFlashToggle = {
                flashMode = if (flashMode == ImageCapture.FLASH_MODE_OFF) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                imageCapture.flashMode = flashMode
            },
            onLocationClick = onLocationClick, // Pass callback
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 40.dp)
        )


        // FRAME OVERLAY
        CameraFrameOverlay(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .aspectRatio(1f)
                .offset(y = (-50).dp)
        )

        CameraBottomSection(
            onCaptureClick = {
                onCapturePhoto(currentLat, currentLng)
            },
            onGalleryClick = {
                galleryLauncher.launch("image/*")
            },
            onFlipCameraClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun CameraTopBar(
    location: String,
    isFlashOn: Boolean,
    onBackClick: () -> Unit,
    onFlashToggle: () -> Unit,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(44.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)) {
                Icon(Icons.Default.ArrowBack, "Back", tint = COLORS_TEXT, modifier = Modifier.size(24.dp))
            }

            Surface(
                color = Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .clickable { onLocationClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = COLORS_PRIMARY, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = location,
                        fontSize = 12.sp,
                        color = COLORS_TEXT,
                        maxLines = 1,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.Edit, null, tint = COLORS_TEXT.copy(alpha = 0.7f), modifier = Modifier.size(12.dp)) // Icon pensil
                }
            }

            IconButton(onClick = onFlashToggle, modifier = Modifier.size(44.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)) {
                Icon(if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff, "Flash", tint = if (isFlashOn) COLORS_PRIMARY else COLORS_TEXT, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun CameraFrameOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        val cornerLength = 40.dp.toPx()
        val dashLength = 15.dp.toPx()
        val gapLength = 15.dp.toPx()
        val primaryColor = Color(0xFFF2C94C)

        val pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(dashLength, gapLength), phase = 0f)

        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            style = Stroke(width = 2.dp.toPx(), pathEffect = pathEffect)
        )

        drawLine(primaryColor, Offset(0f, cornerLength), Offset(0f, 0f), strokeWidth = 6.dp.toPx())
        drawLine(primaryColor, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth = 6.dp.toPx())
        drawLine(primaryColor, Offset(size.width - cornerLength, 0f), Offset(size.width, 0f), strokeWidth = 6.dp.toPx())
        drawLine(primaryColor, Offset(size.width, 0f), Offset(size.width, cornerLength), strokeWidth = 6.dp.toPx())
        drawLine(primaryColor, Offset(0f, size.height - cornerLength), Offset(0f, size.height), strokeWidth = 6.dp.toPx())
        drawLine(primaryColor, Offset(0f, size.height), Offset(cornerLength, size.height), strokeWidth = 6.dp.toPx())
        drawLine(primaryColor, Offset(size.width - cornerLength, size.height), Offset(size.width, size.height), strokeWidth = 6.dp.toPx())
        drawLine(primaryColor, Offset(size.width, size.height - cornerLength), Offset(size.width, size.height), strokeWidth = 6.dp.toPx())
    }
}

@Composable
fun CameraBottomSection(
    onCaptureClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onFlipCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                COLORS_BG.copy(alpha = 0.9f),
                RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .padding(bottom = 32.dp, top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Foto Jalan Berlubang", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = COLORS_TEXT)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Pastikan lubang terlihat jelas dalam kotak area.", fontSize = 12.sp, color = COLORS_TEXT_SECONDARY, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onGalleryClick,
                modifier = Modifier.size(50.dp).background(COLORS_SURFACE, CircleShape).border(1.dp, COLORS_INPUT_BORDER, CircleShape)
            ) {
                Icon(Icons.Default.Image, "Gallery", tint = COLORS_TEXT, modifier = Modifier.size(24.dp))
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(84.dp).border(4.dp, COLORS_TEXT.copy(alpha = 0.2f), CircleShape).padding(6.dp).clip(CircleShape).background(COLORS_PRIMARY).clickable(onClick = onCaptureClick)) {
                Icon(Icons.Default.CameraAlt, "Capture", tint = COLORS_BG, modifier = Modifier.size(36.dp))
            }
            IconButton(
                onClick = onFlipCameraClick,
                modifier = Modifier.size(50.dp).background(COLORS_SURFACE, CircleShape).border(1.dp, COLORS_INPUT_BORDER, CircleShape)
            ) {
                Icon(Icons.Default.Loop, "Flip Camera", tint = COLORS_TEXT, modifier = Modifier.size(24.dp))
            }
        }
    }
}