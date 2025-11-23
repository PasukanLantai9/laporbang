package com.example.laporbang.presentation.view.map

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laporbang.data.model.ReportStatus
import com.example.laporbang.presentation.view.auth.COLORS_BG
import com.example.laporbang.presentation.view.auth.COLORS_PRIMARY
import com.example.laporbang.presentation.view.auth.COLORS_SURFACE
import com.example.laporbang.presentation.view.auth.COLORS_TEXT
import com.example.laporbang.presentation.view.auth.COLORS_TEXT_SECONDARY
import com.example.laporbang.presentation.viewmodel.ReportDetailViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: String,
    onBackClick: () -> Unit,
    onViewOnMapClick: () -> Unit = {},
    viewModel: ReportDetailViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(reportId) {
        viewModel.getReport(reportId)
    }

    val report by viewModel.report.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val operationStatus by viewModel.operationStatus.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(operationStatus) {
        operationStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetOperationStatus()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Laporan?", color = COLORS_TEXT) },
            text = { Text("Tindakan ini tidak dapat dibatalkan.", color = COLORS_TEXT_SECONDARY) },
            containerColor = COLORS_SURFACE,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteReport(onSuccess = onBackClick)
                    showDeleteDialog = false
                }) { Text("Hapus", color = STATUS_RED) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal", color = COLORS_PRIMARY) }
            }
        )
    }

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
                    // TOMBOL DELETE (HANYA ADMIN)
                    if (isAdmin) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = STATUS_RED)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = COLORS_BG)
            )
        },
        bottomBar = {
            if (report != null) {
                Surface(
                    color = COLORS_SURFACE,
                    shadowElevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Share */ },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = COLORS_TEXT)
                        ) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bagikan")
                        }
                        Button(
                            onClick = onViewOnMapClick,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Icon(Icons.Default.Map, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lihat Posisi", color = Color.White)
                        }
                    }
                }
            }
        },
        containerColor = COLORS_BG
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = COLORS_PRIMARY)
            } else if (report != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Image Placeholder
                    Box(
                        modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)).background(Color.Gray)
                    ) {
                        Icon(painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery), null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(48.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION STATUS (EDITABLE JIKA ADMIN)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status Laporan", color = COLORS_TEXT, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        Box {
                            val (color, text) = when(report!!.status) {
                                ReportStatus.BELUM_DITANGANI -> STATUS_RED to "Belum Ditangani"
                                ReportStatus.DALAM_PROSES -> STATUS_YELLOW to "Dalam Proses"
                                ReportStatus.SELESAI -> STATUS_GREEN to "Selesai"
                            }

                            // Chip Status
                            Surface(
                                color = color.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                // Jika Admin, bisa diklik untuk ubah status
                                onClick = { if(isAdmin) showStatusDropdown = true },
                                enabled = isAdmin
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    if (isAdmin) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.ArrowDropDown, null, tint = color, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // DROPDOWN MENU ADMIN
                            DropdownMenu(
                                expanded = showStatusDropdown,
                                onDismissRequest = { showStatusDropdown = false },
                                modifier = Modifier.background(COLORS_SURFACE)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Belum Ditangani", color = STATUS_RED) },
                                    onClick = {
                                        viewModel.updateStatus(ReportStatus.BELUM_DITANGANI)
                                        showStatusDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Dalam Proses", color = STATUS_YELLOW) },
                                    onClick = {
                                        viewModel.updateStatus(ReportStatus.DALAM_PROSES)
                                        showStatusDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Selesai", color = STATUS_GREEN) },
                                    onClick = {
                                        viewModel.updateStatus(ReportStatus.SELESAI)
                                        showStatusDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    DetailInfoCard(title = "ID Laporan", content = "#${report!!.id.take(8).uppercase()}")
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailInfoCard(icon = Icons.Default.LocationOn, title = "Lokasi", content = report!!.address)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) { DetailInfoCard(icon = Icons.Default.CalendarToday, title = "Tanggal", content = report!!.getFormattedDate()) }
                        Box(Modifier.weight(1f)) { DetailInfoCard(icon = Icons.Default.AccessTime, title = "Waktu", content = report!!.getFormattedTime()) }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = COLORS_SURFACE), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Deskripsi", color = COLORS_TEXT, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(report!!.description, color = COLORS_TEXT_SECONDARY, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = COLORS_SURFACE), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Pelapor", fontSize = 12.sp, color = COLORS_TEXT_SECONDARY)
                                Text(report!!.reporterName.ifEmpty { "Anonim" }, fontWeight = FontWeight.Bold, color = COLORS_TEXT)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            } else {
                Text("Laporan tidak ditemukan", color = Color.Red, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun DetailInfoCard(icon: androidx.compose.ui.graphics.vector.ImageVector? = null, title: String, content: String) {
    Card(colors = CardDefaults.cardColors(containerColor = COLORS_SURFACE), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            if (icon != null) { Icon(imageVector = icon, contentDescription = null, tint = COLORS_PRIMARY, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(12.dp)) }
            Column { Text(title, fontSize = 12.sp, color = COLORS_TEXT_SECONDARY); Spacer(modifier = Modifier.height(2.dp)); Text(content, fontWeight = FontWeight.SemiBold, color = COLORS_TEXT, fontSize = 14.sp) }
        }
    }
}