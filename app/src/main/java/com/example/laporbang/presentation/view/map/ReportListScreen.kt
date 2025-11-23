package com.example.laporbang.presentation.view.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
// Import Model Asli
import com.example.laporbang.data.model.Report
import com.example.laporbang.data.model.ReportStatus
import com.example.laporbang.presentation.view.auth.COLORS_BG
import com.example.laporbang.presentation.view.auth.COLORS_PRIMARY
import com.example.laporbang.presentation.view.auth.COLORS_SURFACE
import com.example.laporbang.presentation.view.auth.COLORS_TEXT
import com.example.laporbang.presentation.view.auth.COLORS_TEXT_SECONDARY
import com.example.laporbang.presentation.view.map.STATUS_GREEN
import com.example.laporbang.presentation.view.map.STATUS_RED
import com.example.laporbang.presentation.view.map.STATUS_YELLOW
import com.example.laporbang.presentation.viewmodel.MapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    viewModel: MapViewModel = viewModel(),
    onBackClick: () -> Unit,
    onItemClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    val filteredReports = uiState.filteredReports

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daftar Laporan", fontWeight = FontWeight.Bold, color = COLORS_TEXT) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = COLORS_TEXT)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = COLORS_BG)
            )
        },
        containerColor = COLORS_BG
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { FilterTabItem("Semua", selectedTab == 0) { viewModel.onTabSelected(0) } }
                item { FilterTabItem("Baru", selectedTab == 1) { viewModel.onTabSelected(1) } }
                item { FilterTabItem("Proses", selectedTab == 2) { viewModel.onTabSelected(2) } }
                item { FilterTabItem("Selesai", selectedTab == 3) { viewModel.onTabSelected(3) } }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = COLORS_PRIMARY)
                }
            } else if (filteredReports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada laporan", color = COLORS_TEXT_SECONDARY)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredReports) { report ->
                        ReportListItem(report = report, onClick = { onItemClick(report.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun FilterTabItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) COLORS_PRIMARY else COLORS_SURFACE,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) COLORS_BG else COLORS_TEXT_SECONDARY,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ReportListItem(report: Report, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = COLORS_SURFACE),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray) // Placeholder Image
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val (statusColor, statusText) = when (report.status) {
                    ReportStatus.BELUM_DITANGANI -> STATUS_RED to "Belum Ditangani"
                    ReportStatus.DALAM_PROSES -> STATUS_YELLOW to "Dalam Proses"
                    ReportStatus.SELESAI -> STATUS_GREEN to "Selesai"
                }

                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = report.title,
                    color = COLORS_TEXT,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = COLORS_TEXT_SECONDARY, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = report.getFormattedDate(), color = COLORS_TEXT_SECONDARY, fontSize = 12.sp)
                }
            }
        }
    }
}