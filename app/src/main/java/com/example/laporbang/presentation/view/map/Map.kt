package com.example.laporbang.presentation.view.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
// IMPORT PENTING: Menggunakan Model Asli
import com.example.laporbang.data.model.Report
import com.example.laporbang.data.model.ReportStatus
import com.example.laporbang.presentation.view.auth.COLORS_BG
import com.example.laporbang.presentation.view.auth.COLORS_PRIMARY
import com.example.laporbang.presentation.view.auth.COLORS_SURFACE
import com.example.laporbang.presentation.view.auth.COLORS_TEXT
import com.example.laporbang.presentation.view.auth.COLORS_TEXT_SECONDARY
import com.example.laporbang.presentation.view.auth.COLORS_INPUT_BORDER
import com.example.laporbang.presentation.viewmodel.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

val STATUS_RED = Color(0xFFFF5252)
val STATUS_YELLOW = Color(0xFFFFD740)
val STATUS_GREEN = Color(0xFF69F0AE)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    onCameraClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onViewAllStats: () -> Unit = {},
    onReportClick: (String) -> Unit = {},
    reportIdToFocus: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    var selectedMarker by remember { mutableStateOf<Report?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    )

    val defaultLocation = LatLng(-6.2088, 106.8456)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 13f)
    }

    LaunchedEffect(reportIdToFocus, uiState.reports) {
        if (reportIdToFocus != null && uiState.reports.isNotEmpty()) {
            val target = uiState.reports.find { it.id == reportIdToFocus }
            if (target != null) {
                selectedMarker = target
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(target.latitude, target.longitude), 17f
                    )
                )
            }
        } else {
            if (!locationPermissionsState.allPermissionsGranted) {
                locationPermissionsState.launchMultiplePermissionRequest()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = locationPermissionsState.allPermissionsGranted),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, mapToolbarEnabled = false),
            onMapClick = { selectedMarker = null }
        ) {
            uiState.filteredReports.forEach { report ->
                val hue = when (report.status) {
                    ReportStatus.BELUM_DITANGANI -> BitmapDescriptorFactory.HUE_RED
                    ReportStatus.DALAM_PROSES -> BitmapDescriptorFactory.HUE_ORANGE
                    ReportStatus.SELESAI -> BitmapDescriptorFactory.HUE_GREEN
                }
                Marker(
                    state = MarkerState(position = LatLng(report.latitude, report.longitude)),
                    title = report.title,
                    icon = BitmapDescriptorFactory.defaultMarker(hue),
                    onClick = {
                        selectedMarker = report
                        false
                    }
                )
            }
        }

        AnimatedVisibility(visible = selectedMarker == null, modifier = Modifier.align(Alignment.TopCenter)) {
            Column(modifier = Modifier.fillMaxWidth().systemBarsPadding().padding(top = 16.dp)) {
                MapHeader(onNotificationClick = onNotificationClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(12.dp))
                MapFilterTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.onTabSelected(it) }, // Panggil fungsi ViewModel
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).systemBarsPadding()) {
            if (selectedMarker == null) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FloatingActionButton(
                        onClick = {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    if (location != null) {
                                        scope.launch {
                                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 17f))
                                        }
                                    }
                                }
                            } else { locationPermissionsState.launchMultiplePermissionRequest() }
                        },
                        containerColor = COLORS_SURFACE, contentColor = COLORS_PRIMARY, modifier = Modifier.size(48.dp)
                    ) { Icon(Icons.Default.MyLocation, "My Location") }

                    FloatingActionButton(
                        onClick = onCameraClick,
                        containerColor = COLORS_PRIMARY, contentColor = COLORS_BG, modifier = Modifier.size(64.dp)
                    ) { Icon(Icons.Default.CameraAlt, "Take Photo", modifier = Modifier.size(28.dp)) }

                    MapStatisticsCard(
                        belumCount = uiState.countBelum,
                        prosesCount = uiState.countProses,
                        selesaiCount = uiState.countSelesai,
                        onViewAll = onViewAllStats,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                MapReportPopup(
                    report = selectedMarker!!,
                    onClose = { selectedMarker = null },
                    onDetailClick = { onReportClick(selectedMarker!!.id) }
                )
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = COLORS_PRIMARY)
        }
    }
}


@Composable
fun MapReportPopup(report: Report, onClose: () -> Unit, onDetailClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(16.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)),
        color = COLORS_SURFACE, contentColor = COLORS_TEXT
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    val (color, text) = when (report.status) {
                        ReportStatus.BELUM_DITANGANI -> STATUS_RED to "Belum Ditangani"
                        ReportStatus.DALAM_PROSES -> STATUS_YELLOW to "Dalam Proses"
                        ReportStatus.SELESAI -> STATUS_GREEN to "Selesai"
                    }
                    Surface(color = color.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Text(report.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = COLORS_TEXT)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)) {
                    Icon(Icons.Default.Close, "Close", tint = COLORS_TEXT_SECONDARY, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = COLORS_PRIMARY, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = report.address.ifEmpty { "Lat: ${report.latitude}, Lng: ${report.longitude}" },
                    fontSize = 13.sp, color = COLORS_TEXT_SECONDARY, maxLines = 2
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDetailClick, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = COLORS_PRIMARY)) {
                Text("Lihat Detail", color = COLORS_BG, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null, tint = COLORS_BG, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun MapHeader(
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        color = COLORS_SURFACE,
        contentColor = COLORS_TEXT
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(COLORS_PRIMARY),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "L",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = COLORS_BG
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "LaporBang!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = COLORS_TEXT
                    )
                    Text(
                        text = "Pantau lingkunganmu",
                        fontSize = 12.sp,
                        color = COLORS_TEXT_SECONDARY
                    )
                }
            }

            IconButton(onClick = onNotificationClick) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = COLORS_TEXT_SECONDARY
                )
            }
        }
    }
}

@Composable
fun MapFilterTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            FilterChip(
                label = "Semua",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
        }
        item {
            FilterChip(
                label = "Baru",
                isSelected = selectedTab == 1,
                indicatorColor = STATUS_RED,
                onClick = { onTabSelected(1) }
            )
        }
        item {
            FilterChip(
                label = "Proses",
                isSelected = selectedTab == 2,
                indicatorColor = STATUS_YELLOW,
                onClick = { onTabSelected(2) }
            )
        }
        item {
            FilterChip(
                label = "Selesai",
                isSelected = selectedTab == 3,
                indicatorColor = STATUS_GREEN,
                onClick = { onTabSelected(3) }
            )
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    indicatorColor: Color? = null
) {
    Surface(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        color = if (isSelected) COLORS_PRIMARY else COLORS_SURFACE,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, COLORS_INPUT_BORDER) else null,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (indicatorColor != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) COLORS_BG else COLORS_TEXT_SECONDARY
            )
        }
    }
}

@Composable
fun MapStatisticsCard(
    belumCount: Int,
    prosesCount: Int,
    selesaiCount: Int,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        color = COLORS_SURFACE,
        contentColor = COLORS_TEXT
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Statistik Laporan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = COLORS_TEXT
                )

                Text(
                    text = "Lihat Semua",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = COLORS_PRIMARY,
                    modifier = Modifier.clickable(onClick = onViewAll)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(count = belumCount, label = "Belum", color = STATUS_RED)
                Divider(modifier = Modifier.height(40.dp).width(1.dp), color = COLORS_INPUT_BORDER)
                StatisticItem(count = prosesCount, label = "Proses", color = STATUS_YELLOW)
                Divider(modifier = Modifier.height(40.dp).width(1.dp), color = COLORS_INPUT_BORDER)
                StatisticItem(count = selesaiCount, label = "Selesai", color = STATUS_GREEN)
            }
        }
    }
}

@Composable
fun StatisticItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Text(
            text = count.toString(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = COLORS_TEXT_SECONDARY
        )
    }
}