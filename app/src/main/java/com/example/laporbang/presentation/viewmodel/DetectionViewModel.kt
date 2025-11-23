package com.example.laporbang.presentation.viewmodel

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
    val errorMessage: String? = null,

    val detectedLabel: String = "",
    val confidenceScore: Float = 0f
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

    val timestamp: String = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())

    init {
        startDetectionProcess()
    }

    private fun startDetectionProcess() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentStep = 1, progress = 0.2f)
            delay(1000)

            _uiState.value = _uiState.value.copy(currentStep = 2, progress = 0.5f)

            val result = repository.detectDamage("dummy/path/to/image.jpg")

            delay(500)

            // STEP 4: Selesai & Tampilkan Hasil
            if (result.isSuccess) {
                val data = result.getOrNull()
                _uiState.value = _uiState.value.copy(
                    currentStep = 4,
                    progress = 1.0f,
                    isFinished = true,
                    detectedLabel = "Hasil: ${data?.severity ?: "Terdeteksi"} (${((data?.confidence ?: 0f) * 100).toInt()}%)",
                    confidenceScore = data?.confidence ?: 0f
                )
                if (description.value.isEmpty()) {
                    description.value = data?.description ?: ""
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Gagal mendeteksi lubang. Silakan coba lagi."
                )
            }
        }
    }

    fun setLocation(address: String, lat: Double, lng: Double) {
        currentLocation.value = address
        currentLat = lat
        currentLng = lng
    }

    fun uploadReport() {
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
                imageUrl = ""
            )

            val result = repository.addReport(newReport)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isUploading = false, isUploadSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Gagal mengunggah"
                )
            }
        }
    }

    fun resetUploadState() {
        _uiState.value = _uiState.value.copy(isUploadSuccess = false, errorMessage = null)
    }
}