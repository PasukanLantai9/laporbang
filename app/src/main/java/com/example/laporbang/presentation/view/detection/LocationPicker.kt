package com.example.laporbang.presentation.view.detection

import android.Manifest
import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.laporbang.presentation.view.auth.COLORS_BG
import com.example.laporbang.presentation.view.auth.COLORS_PRIMARY
import com.example.laporbang.presentation.view.auth.COLORS_SURFACE
import com.example.laporbang.presentation.view.auth.COLORS_TEXT
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun LocationPickerScreen(
    onBackClick: () -> Unit,
    onLocationSelected: (String, Double, Double) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Client Lokasi
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // State Izin Lokasi
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Posisi awal (Default Monas, nanti diupdate ke GPS)
    val defaultLocation = LatLng(-6.1753924, 106.8271528)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    // State untuk Search & Address
    var addressResult by remember { mutableStateOf("Menentukan lokasi...") }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Address>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showSearchResults by remember { mutableStateOf(false) }

    // Fungsi pindah kamera ke lokasi saat ini
    fun animateToCurrentLocation() {
        if (locationPermissionsState.allPermissionsGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(userLatLng, 17f)
                            )
                        }
                    } else {
                        Toast.makeText(context, "Sinyal GPS lemah, coba lagi", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SecurityException) {
                // Ignore
            }
        } else {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    // 1. Initial Launch: Minta izin & pindah ke lokasi user
    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        } else {
            // Jika sudah punya izin, langsung ke lokasi user
            animateToCurrentLocation()
        }
    }

    // 2. Logic: Reverse Geocoding (Saat geser peta)
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            val target = cameraPositionState.position.target
            scope.launch(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(target.latitude, target.longitude, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val street = address.thoroughfare ?: address.featureName ?: ""
                        val subLocality = address.subLocality ?: ""
                        val city = address.subAdminArea ?: address.adminArea ?: ""

                        val fullAddress = listOf(street, subLocality, city)
                            .filter { it.isNotEmpty() }
                            .joinToString(", ")

                        withContext(Dispatchers.Main) {
                            addressResult = fullAddress.ifEmpty { "Lokasi terpilih" }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { addressResult = "Gagal memuat alamat" }
                }
            }
        } else {
            addressResult = "Menentukan lokasi..."
        }
    }

    // 3. Logic: Auto-Search saat mengetik (Debounce)
    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) { // Mulai cari setelah 3 huruf
            delay(1000) // Tunggu 1 detik setelah user berhenti mengetik (Debounce)
            isSearching = true

            withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    // Cari maksimal 5 hasil
                    val results = geocoder.getFromLocationName(searchQuery, 5)

                    withContext(Dispatchers.Main) {
                        searchResults = results ?: emptyList()
                        showSearchResults = true
                        isSearching = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { isSearching = false }
                }
            }
        } else {
            showSearchResults = false
        }
    }

    Scaffold(
        bottomBar = {
            // Kartu Konfirmasi di Bawah
            Surface(
                color = COLORS_SURFACE,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Lokasi Terpilih",
                        style = MaterialTheme.typography.labelMedium,
                        color = COLORS_TEXT.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = COLORS_PRIMARY,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = addressResult,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = COLORS_TEXT,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val target = cameraPositionState.position.target
                            onLocationSelected(addressResult, target.latitude, target.longitude)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = COLORS_PRIMARY),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = COLORS_BG)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih Lokasi Ini", color = COLORS_BG, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. GOOGLE MAP
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                properties = MapProperties(isMyLocationEnabled = locationPermissionsState.allPermissionsGranted)
            )

            // 2. CENTER PIN MARKER (Diam di tengah)
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Center Pin",
                tint = Color.Red,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
                    .offset(y = (-24).dp)
            )

            // 3. SEARCH BAR & SUGGESTIONS (Floating di atas)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    color = COLORS_SURFACE
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari alamat...", color = COLORS_TEXT.copy(alpha = 0.5f)) },
                        leadingIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = COLORS_TEXT)
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    showSearchResults = false
                                    focusManager.clearFocus()
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = COLORS_TEXT)
                                }
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = COLORS_TEXT)
                            }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = COLORS_PRIMARY,
                            focusedTextColor = COLORS_TEXT,
                            unfocusedTextColor = COLORS_TEXT
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            keyboardController?.hide()
                            showSearchResults = false
                        })
                    )
                }

                // --- LIST HASIL PENCARIAN (POPUP/MARKDOWN) ---
                AnimatedVisibility(visible = showSearchResults && searchResults.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .heightIn(max = 200.dp) // Batasi tinggi
                            .shadow(8.dp, RoundedCornerShape(12.dp)),
                        color = COLORS_SURFACE,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        LazyColumn {
                            items(searchResults) { address ->
                                val addressLine = address.getAddressLine(0) ?: "Lokasi tanpa nama"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Saat item diklik
                                            val latLng = LatLng(address.latitude, address.longitude)
                                            scope.launch {
                                                cameraPositionState.animate(
                                                    CameraUpdateFactory.newLatLngZoom(latLng, 17f)
                                                )
                                            }
                                            searchQuery = addressLine // Set text jadi alamat yg dipilih
                                            showSearchResults = false // Tutup list
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, null, tint = COLORS_TEXT.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = addressLine,
                                        color = COLORS_TEXT,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            // Loading Indicator Pencarian
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 80.dp),
                    color = COLORS_PRIMARY,
                    strokeWidth = 3.dp
                )
            }

            // 4. TOMBOL MY LOCATION (Custom FAB)
            FloatingActionButton(
                onClick = { animateToCurrentLocation() },
                containerColor = COLORS_SURFACE,
                contentColor = COLORS_PRIMARY,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 160.dp, end = 16.dp) // Di atas kartu konfirmasi
                    .size(50.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }
    }
}