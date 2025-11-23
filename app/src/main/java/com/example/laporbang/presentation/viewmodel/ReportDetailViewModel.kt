package com.example.laporbang.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laporbang.data.model.Report
import com.example.laporbang.data.model.ReportStatus
import com.example.laporbang.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportDetailViewModel(
    private val repository: ReportRepository = ReportRepository()
) : ViewModel() {

    private val _report = MutableStateFlow<Report?>(null)
    val report: StateFlow<Report?> = _report

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // State untuk Admin
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    private val _operationStatus = MutableStateFlow<String?>(null)
    val operationStatus: StateFlow<String?> = _operationStatus

    fun getReport(id: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val role = repository.getUserRole()
            _isAdmin.value = (role == "admin")

            val result = repository.getReportById(id)
            if (result.isSuccess) {
                _report.value = result.getOrNull()
            }
            _isLoading.value = false
        }
    }

    fun updateStatus(newStatus: ReportStatus) {
        val currentReport = _report.value ?: return
        viewModelScope.launch {
            val result = repository.updateReportStatus(currentReport.id, newStatus)
            if (result.isSuccess) {
                _report.value = currentReport.copy(status = newStatus)
                _operationStatus.value = "Status berhasil diperbarui"
            } else {
                _operationStatus.value = "Gagal memperbarui status"
            }
        }
    }

    fun deleteReport(onSuccess: () -> Unit) {
        val currentReport = _report.value ?: return
        viewModelScope.launch {
            val result = repository.deleteReport(currentReport.id)
            if (result.isSuccess) {
                _operationStatus.value = "Laporan dihapus"
                onSuccess()
            } else {
                _operationStatus.value = "Gagal menghapus laporan"
            }
        }
    }

    fun resetOperationStatus() {
        _operationStatus.value = null
    }
}