package com.example.laporbang.presentation.view.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
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
import com.example.laporbang.presentation.view.auth.COLORS_BG
import com.example.laporbang.presentation.view.auth.COLORS_PRIMARY
import com.example.laporbang.presentation.view.auth.COLORS_SURFACE
import com.example.laporbang.presentation.view.auth.COLORS_TEXT
import com.example.laporbang.presentation.view.auth.COLORS_TEXT_SECONDARY
import com.example.laporbang.presentation.view.auth.COLORS_INPUT_BORDER
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

data class ReportMarker(
    val id: String,
    val position: LatLng,
    val status: ReportStatus,
    val title: String = ""
)

enum class ReportStatus {
    BELUM_DITANGANI,
    DALAM_PROSES,
    SELESAI
}

val STATUS_RED = Color(0xFFFF5252)
val STATUS_YELLOW = Color(0xFFFFD740)
val STATUS_GREEN = Color(0xFF69F0AE)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    onCameraClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onViewAllStats: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    val markers = remember {
        listOf(
            ReportMarker("1", LatLng(-7.9666, 112.6326), ReportStatus.BELUM_DITANGANI, "Jl. Ijen"),
            ReportMarker("2", LatLng(-7.9556, 112.6146), ReportStatus.BELUM_DITANGANI, "Jl. Soekarno Hatta"),
            ReportMarker("3", LatLng(-7.9756, 112.6426), ReportStatus.SELESAI, "Alun-alun"),
            ReportMarker("4", LatLng(-7.9456, 112.6226), ReportStatus.SELESAI, "Lowokwaru"),
            ReportMarker("5", LatLng(-7.9856, 112.6526), ReportStatus.DALAM_PROSES, "Sukun")
        )
    }

    val filteredMarkers = remember(selectedTab, markers) {
        when (selectedTab) {
            0 -> markers
            1 -> markers.filter { it.status == ReportStatus.BELUM_DITANGANI }
            2 -> markers.filter { it.status == ReportStatus.DALAM_PROSES }
            3 -> markers.filter { it.status == ReportStatus.SELESAI }
            else -> markers
        }
    }

    val statsData = remember {
        mapOf(
            "Belum" to markers.count { it.status == ReportStatus.BELUM_DITANGANI },
            "Proses" to markers.count { it.status == ReportStatus.DALAM_PROSES },
            "Selesai" to markers.count { it.status == ReportStatus.SELESAI }
        )
    }

    val mapProperties = remember(locationPermissionsState.allPermissionsGranted) {
        MapProperties(isMyLocationEnabled = locationPermissionsState.allPermissionsGranted)
    }
    val mapUiSettings = remember {
        MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false
        )
    }

    val defaultLocation = LatLng(-7.9666, 112.6326)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 13f)
    }

    LaunchedEffect(Unit) {
        locationPermissionsState.launchMultiplePermissionRequest()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            filteredMarkers.forEach { marker ->
                val hue = when (marker.status) {
                    ReportStatus.BELUM_DITANGANI -> BitmapDescriptorFactory.HUE_RED
                    ReportStatus.DALAM_PROSES -> BitmapDescriptorFactory.HUE_ORANGE
                    ReportStatus.SELESAI -> BitmapDescriptorFactory.HUE_GREEN
                }
                Marker(
                    state = MarkerState(position = marker.position),
                    title = marker.title,
                    icon = BitmapDescriptorFactory.defaultMarker(hue)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .systemBarsPadding()
                .padding(top = 16.dp)
        ) {
            MapHeader(
                onNotificationClick = onNotificationClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            MapFilterTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .systemBarsPadding()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    scope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(location.latitude, location.longitude),
                                                17f
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            locationPermissionsState.launchMultiplePermissionRequest()
                        }
                    },
                    containerColor = COLORS_SURFACE,
                    contentColor = COLORS_PRIMARY,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }

                FloatingActionButton(
                    onClick = onCameraClick,
                    containerColor = COLORS_PRIMARY,
                    contentColor = COLORS_BG,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Take Photo",
                        modifier = Modifier.size(28.dp)
                    )
                }

                MapStatisticsCard(
                    belumCount = statsData["Belum"] ?: 0,
                    prosesCount = statsData["Proses"] ?: 0,
                    selesaiCount = statsData["Selesai"] ?: 0,
                    onViewAll = onViewAllStats,
                    modifier = Modifier.fillMaxWidth()
                )
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