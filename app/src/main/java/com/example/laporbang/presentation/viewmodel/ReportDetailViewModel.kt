package com.example.laporbang.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laporbang.data.model.Report
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

    fun getReport(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getReportById(id)
            if (result.isSuccess) {
                _report.value = result.getOrNull()
            }
            _isLoading.value = false
        }
    }
}