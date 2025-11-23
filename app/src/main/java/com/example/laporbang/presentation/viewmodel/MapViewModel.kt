package com.example.laporbang.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laporbang.data.model.Report
import com.example.laporbang.data.model.ReportStatus
import com.example.laporbang.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val reports: List<Report> = emptyList(),
    val filteredReports: List<Report> = emptyList(),
    val isLoading: Boolean = true,
    val countBelum: Int = 0,
    val countProses: Int = 0,
    val countSelesai: Int = 0
)

class MapViewModel(
    private val repository: ReportRepository = ReportRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        fetchReports()
    }

    private fun fetchReports() {
        viewModelScope.launch {
            repository.getReports().collect { list ->
                val belum = list.count { it.status == ReportStatus.BELUM_DITANGANI }
                val proses = list.count { it.status == ReportStatus.DALAM_PROSES }
                val selesai = list.count { it.status == ReportStatus.SELESAI }

                _uiState.value = _uiState.value.copy(
                    reports = list,
                    isLoading = false,
                    countBelum = belum,
                    countProses = proses,
                    countSelesai = selesai
                )
                filterReports(_selectedTab.value)
            }
        }
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
        filterReports(index)
    }

    private fun filterReports(index: Int) {
        val all = _uiState.value.reports
        val filtered = when (index) {
            1 -> all.filter { it.status == ReportStatus.BELUM_DITANGANI }
            2 -> all.filter { it.status == ReportStatus.DALAM_PROSES }
            3 -> all.filter { it.status == ReportStatus.SELESAI }
            else -> all
        }
        _uiState.value = _uiState.value.copy(filteredReports = filtered)
    }
}