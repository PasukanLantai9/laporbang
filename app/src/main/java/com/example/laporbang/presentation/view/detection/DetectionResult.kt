package com.example.laporbang.presentation.view.detection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laporbang.presentation.view.auth.COLORS_BG
import com.example.laporbang.presentation.view.auth.COLORS_PRIMARY
import com.example.laporbang.presentation.view.auth.COLORS_SURFACE
import com.example.laporbang.presentation.view.auth.COLORS_TEXT
import com.example.laporbang.presentation.view.auth.COLORS_TEXT_SECONDARY
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionResultScreen(
    initialLocation: String,
    onBackClick: () -> Unit,
    onChangeLocationClick: () -> Unit,
    onUploadClick: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var currentStep by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    val timestamp = remember {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) // Locale Indonesia
        sdf.format(Date())
    }

    var displayLocation by remember { mutableStateOf(initialLocation) }

    LaunchedEffect(Unit) {
        currentStep = 1
        delay(1000)
        progress = 0.3f

        currentStep = 2
        delay(1500)
        progress = 0.6f

        currentStep = 3
        delay(2000)
        progress = 1.0f

        currentStep = 4
        isFinished = true
    }

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Deteksi Lubang",
                        fontWeight = FontWeight.Bold,
                        color = COLORS_TEXT
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = COLORS_TEXT)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = COLORS_BG
                )
            )
        },
        bottomBar = {
            Surface(
                color = COLORS_SURFACE,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isFinished) {
                        Button(
                            onClick = onUploadClick,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = COLORS_PRIMARY),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Unggah Laporan", color = COLORS_BG, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onBackClick,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color(0xFFEF4444)
                            ),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray)
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_camera),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center).size(64.dp)
                    )

                    // Overlay kotak deteksi (Hiasan)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(150.dp)
                            .border(2.dp, COLORS_PRIMARY, RoundedCornerShape(8.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = COLORS_SURFACE),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(if (isFinished) Color(0xFF10B981) else COLORS_PRIMARY.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFinished) Icons.Default.Check else Icons.Default.Search,
                            contentDescription = null,
                            tint = if (isFinished) Color.White else COLORS_PRIMARY,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isFinished) "Deteksi Selesai" else "Mendeteksi Lubang...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = COLORS_TEXT
                    )
                    Text(
                        text = if (isFinished) "Kerusakan jalan berhasil diidentifikasi." else "Menganalisis gambar untuk\nmengidentifikasi kerusakan jalan",
                        fontSize = 13.sp,
                        color = COLORS_TEXT_SECONDARY,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Progress", fontSize = 12.sp, color = COLORS_TEXT_SECONDARY)
                        Text("${(animatedProgress * 100).toInt()}%", fontSize = 12.sp, color = COLORS_PRIMARY, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = COLORS_PRIMARY,
                        trackColor = Color(0xFF374151)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    DetectionStepItem(
                        label = "Memuat gambar",
                        state = if (currentStep > 1) StepState.DONE else if (currentStep == 1) StepState.LOADING else StepState.WAITING
                    )
                    DetectionStepItem(
                        label = "Preprocessing gambar",
                        state = if (currentStep > 2) StepState.DONE else if (currentStep == 2) StepState.LOADING else StepState.WAITING
                    )
                    DetectionStepItem(
                        label = "Analisis AI",
                        state = if (currentStep > 3) StepState.DONE else if (currentStep == 3) StepState.LOADING else StepState.WAITING
                    )

                    AnimatedVisibility(visible = isFinished) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            DetectionStepItem(
                                label = "Hasil: Lubang Parah (Confidence: 98%)",
                                state = StepState.DONE,
                                isResult = true
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isFinished) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = COLORS_SURFACE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
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
                        onValueChange = { description = it },
                        label = { Text("Deskripsi Tambahan", color = COLORS_TEXT_SECONDARY) },
                        placeholder = { Text("Cth: Lubang sedalam 10cm di tengah jalan", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = COLORS_PRIMARY,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = COLORS_TEXT,
                            unfocusedTextColor = COLORS_TEXT,
                            cursorColor = COLORS_PRIMARY
                        ),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = COLORS_SURFACE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Time",
                                tint = COLORS_PRIMARY,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Waktu Pelaporan", fontSize = 12.sp, color = COLORS_TEXT_SECONDARY)
                                Text(
                                    text = timestamp,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = COLORS_TEXT
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A).copy(alpha = 0.3f)), // Biru tua transparan
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = Color(0xFF60A5FA), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Tips Deteksi", fontWeight = FontWeight.Bold, color = Color(0xFF93C5FD))
                        Text(
                            "Pastikan pencahayaan cukup dan ambil gambar dari jarak 1-2 meter agar hasil akurat.",
                            fontSize = 12.sp,
                            color = Color(0xFFBFDBFE)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

enum class StepState { WAITING, LOADING, DONE }

@Composable
fun DetectionStepItem(label: String, state: StepState, isResult: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        when (state) {
            StepState.DONE -> Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if(isResult) COLORS_PRIMARY else Color(0xFF10B981),
                modifier = Modifier.size(20.dp)
            )
            StepState.LOADING -> CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = COLORS_PRIMARY,
                strokeWidth = 2.dp
            )
            StepState.WAITING -> Icon(
                painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_help), // Placeholder dot
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            color = if (state == StepState.WAITING) Color.Gray else COLORS_TEXT,
            fontSize = 14.sp,
            fontWeight = if(isResult) FontWeight.Bold else FontWeight.Normal
        )
    }
}