package com.example.laporbang.presentation.view

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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Loop // Icon untuk flip camera
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Pastikan import warna sesuai dengan lokasi file kamu
import com.example.laporbang.presentation.view.auth.COLORS_BG
import com.example.laporbang.presentation.view.auth.COLORS_INPUT_BORDER
import com.example.laporbang.presentation.view.auth.COLORS_PRIMARY
import com.example.laporbang.presentation.view.auth.COLORS_SURFACE
import com.example.laporbang.presentation.view.auth.COLORS_TEXT
import com.example.laporbang.presentation.view.auth.COLORS_TEXT_SECONDARY

@Composable
fun CameraScreen(
    onBackClick: () -> Unit = {},
    onCapturePhoto: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
    onFlashToggle: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var isFlashOn by remember { mutableStateOf(false) }
    // Simulasi lokasi dummy
    val currentLocation by remember { mutableStateOf("Jl. Sudirman, Jakarta Pusat") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(COLORS_BG) // Gunakan warna background aplikasi
    ) {
        // 1. Camera Preview (Placeholder Hitam/Gelap biar realistis)
        CameraPreviewPlaceholder()

        // 2. Top Bar (Tombol Back, Flash, Setting)
        CameraTopBar(
            location = currentLocation,
            isFlashOn = isFlashOn,
            onBackClick = onBackClick,
            onFlashToggle = {
                isFlashOn = !isFlashOn
                onFlashToggle()
            },
            onSettingsClick = onSettingsClick,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 16.dp) // Safe area padding
        )

        // 3. Frame Overlay (Garis putus-putus warna Primary)
        CameraFrameOverlay(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .aspectRatio(1f) // Kotak persegi atau sesuaikan rasio
                .offset(y = (-50).dp) // Sedikit ke atas biar ga ketutup panel bawah
        )

        // 4. Bottom Section (Panel Kontrol Utama)
        CameraBottomSection(
            onCaptureClick = onCapturePhoto,
            onGalleryClick = onGalleryClick,
            onFlipCameraClick = { /* Flip logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun CameraPreviewPlaceholder() {
    // Placeholder warna abu-abu gelap (simulasi viewfinder mati)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2D2D2D))
    ) {
        Text(
            text = "Camera Preview",
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun CameraTopBar(
    location: String,
    isFlashOn: Boolean,
    onBackClick: () -> Unit,
    onFlashToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Row Tombol Atas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tombol Back
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = COLORS_TEXT,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Lokasi (Tengah)
            Surface(
                color = Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = COLORS_PRIMARY,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = location,
                        fontSize = 12.sp,
                        color = COLORS_TEXT,
                        maxLines = 1
                    )
                }
            }

            // Tombol Kanan (Flash & Settings)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onFlashToggle,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = if (isFlashOn) COLORS_PRIMARY else COLORS_TEXT,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = COLORS_TEXT,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CameraFrameOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        val cornerLength = 40.dp.toPx() // Panjang siku sudut
        val dashLength = 15.dp.toPx()
        val gapLength = 15.dp.toPx()

        // Warna Primary (Kuning)
        val primaryColor = Color(0xFFF2C94C)

        // 1. Gambar Garis Putus-putus (Border Tipis)
        val pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(dashLength, gapLength),
            phase = 0f
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f), // Putih transparan biar tidak dominan
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            style = Stroke(width = 2.dp.toPx(), pathEffect = pathEffect)
        )

        // 2. Gambar Siku Sudut (Tebal & Solid warna Primary)
        val cornerStroke = Stroke(width = strokeWidth * 1.5f)

        // Kiri Atas
        drawLine(primaryColor, Offset(0f, cornerLength), Offset(0f, 0f), strokeWidth = 6.dp.toPx())
        drawLine(primaryColor, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth = 6.dp.toPx())

        // Kanan Atas
        drawLine(primaryColor, Offset(size.width - cornerLength, 0f), Offset(size.width, 0f), strokeWidth = 6.dp.toPx())
        drawLine(primaryColor, Offset(size.width, 0f), Offset(size.width, cornerLength), strokeWidth = 6.dp.toPx())

        // Kiri Bawah
        drawLine(primaryColor, Offset(0f, size.height - cornerLength), Offset(0f, size.height), strokeWidth = 6.dp.toPx())
        drawLine(primaryColor, Offset(0f, size.height), Offset(cornerLength, size.height), strokeWidth = 6.dp.toPx())

        // Kanan Bawah
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
                COLORS_BG.copy(alpha = 0.95f), // Semi transparan agar menyatu
                RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .padding(bottom = 32.dp, top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Judul & Instruksi
        Text(
            text = "Foto Jalan Berlubang",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = COLORS_TEXT
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pastikan lubang terlihat jelas dalam kotak area.",
            fontSize = 12.sp,
            color = COLORS_TEXT_SECONDARY,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Row Tombol Kontrol
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Tombol Galeri (Kiri)
            IconButton(
                onClick = onGalleryClick,
                modifier = Modifier
                    .size(50.dp)
                    .background(COLORS_SURFACE, CircleShape)
                    .border(1.dp, COLORS_INPUT_BORDER, CircleShape)
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Gallery",
                    tint = COLORS_TEXT,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 2. Tombol Shutter (Tengah - Besar)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(84.dp)
                    .border(4.dp, COLORS_TEXT.copy(alpha = 0.2f), CircleShape) // Ring luar tipis
                    .padding(6.dp) // Jarak antara ring dan tombol
                    .clip(CircleShape)
                    .background(COLORS_PRIMARY) // Warna Kuning
                    .clickable(onClick = onCaptureClick)
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Capture",
                    tint = COLORS_BG, // Icon warna gelap biar kontras
                    modifier = Modifier.size(36.dp)
                )
            }

            // 3. Tombol Flip Camera (Kanan)
            IconButton(
                onClick = onFlipCameraClick,
                modifier = Modifier
                    .size(50.dp)
                    .background(COLORS_SURFACE, CircleShape)
                    .border(1.dp, COLORS_INPUT_BORDER, CircleShape)
            ) {
                Icon(
                    Icons.Default.Loop, // Gunakan icon Loop/Refresh untuk flip
                    contentDescription = "Flip Camera",
                    tint = COLORS_TEXT,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}