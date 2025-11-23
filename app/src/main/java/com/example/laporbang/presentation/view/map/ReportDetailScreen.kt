package com.example.laporbang.presentation.view.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laporbang.presentation.view.auth.COLORS_BG
import com.example.laporbang.presentation.view.auth.COLORS_PRIMARY
import com.example.laporbang.presentation.view.auth.COLORS_SURFACE
import com.example.laporbang.presentation.view.auth.COLORS_TEXT
import com.example.laporbang.presentation.view.auth.COLORS_TEXT_SECONDARY
import com.example.laporbang.presentation.view.map.STATUS_RED

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: String,
    onBackClick: () -> Unit,
    onViewOnMapClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detail Laporan", fontWeight = FontWeight.Bold, color = COLORS_TEXT) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = COLORS_TEXT)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = COLORS_TEXT)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Share Logic */ },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = COLORS_TEXT)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bagikan")
                    }

                    // --- TOMBOL LIHAT POSISI (UPDATED) ---
                    Button(
                        onClick = onViewOnMapClick,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)) // Biru Maps
                    ) {
                        Icon(Icons.Default.Map, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lihat Posisi", color = Color.White)
                    }
                }
            }
        },
        containerColor = COLORS_BG
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. Gambar Laporan Besar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray) // Placeholder Image
            ) {
                // Nanti ganti dengan Image dari URL
                Icon(
                    painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.Center).size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Status & ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Status Laporan", color = COLORS_TEXT, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(
                    color = STATUS_RED.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Belum Ditangani",
                        color = STATUS_RED,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DetailInfoCard(
                title = "ID Laporan",
                content = "#RPT-2024-00$reportId"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Lokasi
            DetailInfoCard(
                icon = Icons.Default.LocationOn,
                title = "Lokasi",
                content = "Jl. Jenderal Sudirman No. 45, Kec. Menteng, Jakarta Pusat, DKI Jakarta 10310"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Tanggal & Waktu
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    DetailInfoCard(icon = Icons.Default.CalendarToday, title = "Tanggal", content = "15 Nov 2024")
                }
                Box(modifier = Modifier.weight(1f)) {
                    DetailInfoCard(icon = Icons.Default.AccessTime, title = "Waktu", content = "14:30 WIB")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Deskripsi
            Card(
                colors = CardDefaults.cardColors(containerColor = COLORS_SURFACE),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Deskripsi", color = COLORS_TEXT, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lubang berukuran sedang di tengah jalan dengan kedalaman sekitar 15cm. Berpotensi membahayakan pengendara motor dan mobil yang melintas.",
                        color = COLORS_TEXT_SECONDARY,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Pelapor
            Card(
                colors = CardDefaults.cardColors(containerColor = COLORS_SURFACE),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray)
                    ) // Avatar Placeholder

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text("Pelapor", fontSize = 12.sp, color = COLORS_TEXT_SECONDARY)
                        Text("Ahmad Rizki", fontWeight = FontWeight.Bold, color = COLORS_TEXT)
                        Text("Warga Jakarta Pusat", fontSize = 12.sp, color = COLORS_TEXT_SECONDARY)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun DetailInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    title: String,
    content: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = COLORS_SURFACE),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (icon != null) {
                Icon(icon, null, tint = COLORS_PRIMARY, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(title, fontSize = 12.sp, color = COLORS_TEXT_SECONDARY)
                Spacer(modifier = Modifier.height(2.dp))
                Text(content, fontWeight = FontWeight.SemiBold, color = COLORS_TEXT, fontSize = 14.sp)
            }
        }
    }
}