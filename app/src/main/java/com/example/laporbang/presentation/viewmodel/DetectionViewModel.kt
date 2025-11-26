package com.example.laporbang.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laporbang.data.model.Report
import com.example.laporbang.data.model.ReportStatus
import com.example.laporbang.data.repository.ReportRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DetectionUiState(
    val progress: Float = 0f,
    val currentStep: Int = 0,
    val isFinished: Boolean = false,
    val isUploading: Boolean = false,
    val isUploadSuccess: Boolean = false,
    val detectedLabel: String = "",
    val confidenceScore: Float = 0f,
    val isPotholeFound: Boolean = false,
    val errorMessage: String? = null
)

class DetectionViewModel(
    private val repository: ReportRepository = ReportRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetectionUiState())
    val uiState: StateFlow<DetectionUiState> = _uiState.asStateFlow()

    var description = MutableStateFlow("")
    var currentLocation = MutableStateFlow("")

    private var currentLat = 0.0
    private var currentLng = 0.0
    private var imageUriString: String = ""

    val timestamp: String = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())

    // Fungsi Utama: Memulai Deteksi
    fun startDetection(context: Context, uriString: String) {
        imageUriString = uriString
        val uri = Uri.parse(uriString)

        viewModelScope.launch {
            // Reset State
            _uiState.value = DetectionUiState(currentStep = 1, progress = 0.1f)

            // Step 1: Load Image (Simulasi)
            delay(800)
            _uiState.value = _uiState.value.copy(currentStep = 2, progress = 0.4f)

            // Step 2: Preprocessing (Simulasi)
            delay(800)
            _uiState.value = _uiState.value.copy(currentStep = 3, progress = 0.6f) // Mulai hit API

            // Step 3: Hit API Asli
            val result = repository.detectDamage(context, uri)

            // Animasi selesai
            _uiState.value = _uiState.value.copy(progress = 1.0f)

            if (result.isSuccess) {
                val data = result.getOrNull()

                if (data != null && data.isPotholeDetected) {
                    _uiState.value = _uiState.value.copy(
                        currentStep = 4,
                        isFinished = true,
                        isPotholeFound = true,
                        detectedLabel = "Terdeteksi: ${data.potholeCount} Lubang",
                    )
                    if (description.value.isEmpty()) {
                        description.value = data.description
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        currentStep = 4,
                        isFinished = true,
                        isPotholeFound = false,
                        detectedLabel = "Tidak ditemukan lubang",
                        errorMessage = "Objek tidak terdeteksi sebagai lubang jalan. Laporan tidak dapat dibuat."
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    currentStep = 4,
                    isFinished = true,
                    isPotholeFound = false,
                    detectedLabel = "Gagal Analisis",
                    errorMessage = result.exceptionOrNull()?.message ?: "Terjadi kesalahan server"
                )
            }
        }
    }

    fun setLocation(address: String, lat: Double, lng: Double) {
        if (currentLocation.value.isEmpty() || currentLocation.value != address) {
            currentLocation.value = address
            currentLat = lat
            currentLng = lng
        }
    }

    fun uploadReport() {
        // Cegah upload jika tidak valid
        if (!_uiState.value.isPotholeFound) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true)

            val newReport = Report(
                title = "Laporan Kerusakan Jalan",
                description = description.value,
                address = currentLocation.value,
                latitude = currentLat,
                longitude = currentLng,
                location = GeoPoint(currentLat, currentLng),
                status = ReportStatus.BELUM_DITANGANI,
                reporterName = "User LaporBang",
                createdAt = Timestamp.now(),
                imageUrl = imageUriString
            )

            val result = repository.addReport(newReport)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isUploading = false, isUploadSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun resetUploadState() {
        _uiState.value = _uiState.value.copy(isUploadSuccess = false, errorMessage = null)
    }
}