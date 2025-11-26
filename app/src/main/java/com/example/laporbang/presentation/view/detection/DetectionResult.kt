package com.example.laporbang.presentation.view.detection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.laporbang.presentation.view.auth.COLORS_BG
import com.example.laporbang.presentation.view.auth.COLORS_PRIMARY
import com.example.laporbang.presentation.view.auth.COLORS_SURFACE
import com.example.laporbang.presentation.view.auth.COLORS_TEXT
import com.example.laporbang.presentation.view.auth.COLORS_TEXT_SECONDARY
import com.example.laporbang.presentation.viewmodel.DetectionViewModel
import com.example.laporbang.presentation.viewmodel.DetectionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionResultScreen(
    initialLocation: String,
    initialLat: Double = 0.0,
    initialLng: Double = 0.0,
    imageUriString: String = "", // Parameter URI Foto
    onBackClick: () -> Unit,
    onChangeLocationClick: () -> Unit,
    onUploadSuccess: () -> Unit,
    viewModel: DetectionViewModel = viewModel()
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()
    val description by viewModel.description.collectAsState()
    val displayLocation by viewModel.currentLocation.collectAsState()
    val animatedProgress by animateFloatAsState(targetValue = uiState.progress, label = "progress")

    LaunchedEffect(initialLocation) {
        viewModel.setLocation(initialLocation, initialLat, initialLng)
    }

    LaunchedEffect(imageUriString) {
        if (imageUriString.isNotEmpty() && uiState.currentStep == 0) {
            viewModel.startDetection(context, imageUriString)
        }
    }

    // 3. Handle Upload Sukses
    LaunchedEffect(uiState.isUploadSuccess) {
        if (uiState.isUploadSuccess) {
            onUploadSuccess()
            viewModel.resetUploadState()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Deteksi Lubang", fontWeight = FontWeight.Bold, color = COLORS_TEXT) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = COLORS_TEXT)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = COLORS_BG)
            )
        },
        bottomBar = {
            Surface(
                color = COLORS_SURFACE,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.isFinished) {
                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = Color(0xFFEF4444),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            )
                        }
                        Button(
                            onClick = { viewModel.uploadReport() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isPotholeFound) COLORS_PRIMARY else Color.Gray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = uiState.isPotholeFound && !uiState.isUploading // Disable jika bukan lubang
                        ) {
                            if (uiState.isUploading) {
                                CircularProgressIndicator(color = COLORS_BG, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Unggah Laporan", color = COLORS_BG, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFEF4444)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batalkan Deteksi", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(COLORS_BG)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(250.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Gray)) {
                    if (imageUriString.isNotEmpty()) {
                        AsyncImage(
                            model = imageUriString,
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_camera), null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(64.dp))
                    }

                    Box(modifier = Modifier.align(Alignment.Center).size(150.dp).border(2.dp, COLORS_PRIMARY, RoundedCornerShape(8.dp)))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = COLORS_SURFACE),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Icon Status (Berubah warna jika gagal/berhasil)
                    val statusColor = when {
                        !uiState.isFinished -> COLORS_PRIMARY.copy(alpha = 0.2f)
                        uiState.isPotholeFound -> Color(0xFF10B981) // Hijau (Sukses)
                        else -> Color(0xFFEF4444) // Merah (Bukan Lubang)
                    }

                    val statusIcon = when {
                        !uiState.isFinished -> Icons.Default.Search
                        uiState.isPotholeFound -> Icons.Default.Check
                        else -> Icons.Default.Close
                    }

                    Box(
                        modifier = Modifier.size(60.dp).clip(CircleShape).background(statusColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = statusIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (uiState.isFinished) "Deteksi Selesai" else "Mendeteksi Lubang...",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = COLORS_TEXT
                    )
                    Text(
                        text = if (uiState.isFinished) "Proses analisis gambar telah selesai." else "Menganalisis gambar untuk\nmengidentifikasi kerusakan jalan",
                        fontSize = 13.sp, color = COLORS_TEXT_SECONDARY, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progress", fontSize = 12.sp, color = COLORS_TEXT_SECONDARY)
                        Text("${(animatedProgress * 100).toInt()}%", fontSize = 12.sp, color = COLORS_PRIMARY, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = COLORS_PRIMARY, trackColor = Color(0xFF374151)
                    )

                    Spacer(modifier = Modifier.height(24.dp))


                    DetectionStepItem("Memuat gambar", if (uiState.currentStep > 1) StepState.DONE else if (uiState.currentStep == 1) StepState.LOADING else StepState.WAITING)
                    DetectionStepItem("Preprocessing gambar", if (uiState.currentStep > 2) StepState.DONE else if (uiState.currentStep == 2) StepState.LOADING else StepState.WAITING)
                    DetectionStepItem("Analisis AI", if (uiState.currentStep > 3) StepState.DONE else if (uiState.currentStep == 3) StepState.LOADING else StepState.WAITING)

                    AnimatedVisibility(visible = uiState.isFinished) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {

                            DetectionStepItem(
                                label = uiState.detectedLabel.ifEmpty { "Analisis Selesai" },
                                state = if(uiState.isPotholeFound) StepState.DONE else StepState.ERROR,
                                isResult = true
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = uiState.isFinished && uiState.isPotholeFound) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = COLORS_SURFACE), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Lokasi Kejadian", fontWeight = FontWeight.Bold, color = COLORS_TEXT, fontSize = 16.sp)
                                TextButton(onClick = onChangeLocationClick) {
                                    Icon(Icons.Default.Edit, null, tint = COLORS_PRIMARY, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ubah", color = COLORS_PRIMARY, fontSize = 13.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.LocationOn, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = displayLocation, color = COLORS_TEXT_SECONDARY, fontSize = 14.sp, lineHeight = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { viewModel.description.value = it },
                        label = { Text("Deskripsi Tambahan", color = COLORS_TEXT_SECONDARY) },
                        placeholder = { Text("Cth: Lubang sedalam 10cm...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = COLORS_PRIMARY, unfocusedBorderColor = Color.Gray,
                            focusedTextColor = COLORS_TEXT, unfocusedTextColor = COLORS_TEXT, cursorColor = COLORS_PRIMARY
                        ),
                        minLines = 3, maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = COLORS_SURFACE), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, "Time", tint = COLORS_PRIMARY, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Waktu Pelaporan", fontSize = 12.sp, color = COLORS_TEXT_SECONDARY)
                                Text(text = viewModel.timestamp, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = COLORS_TEXT)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

enum class StepState { WAITING, LOADING, DONE, ERROR }

@Composable
fun DetectionStepItem(label: String, state: StepState, isResult: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        when (state) {
            StepState.DONE -> Icon(Icons.Default.CheckCircle, null, tint = if(isResult) COLORS_PRIMARY else Color(0xFF10B981), modifier = Modifier.size(20.dp))
            StepState.LOADING -> CircularProgressIndicator(modifier = Modifier.size(16.dp), color = COLORS_PRIMARY, strokeWidth = 2.dp)
            StepState.WAITING -> Icon(painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_help), null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            StepState.ERROR -> Icon(Icons.Default.Close, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, color = if (state == StepState.WAITING) Color.Gray else if(state == StepState.ERROR) Color(0xFFEF4444) else COLORS_TEXT, fontSize = 14.sp, fontWeight = if(isResult) FontWeight.Bold else FontWeight.Normal)
    }
}