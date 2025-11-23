package com.example.laporbang.data.model

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

enum class ReportStatus {
    BELUM_DITANGANI,
    DALAM_PROSES,
    SELESAI
}

data class Report(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: ReportStatus = ReportStatus.BELUM_DITANGANI,
    val imageUrl: String = "",
    val reporterName: String = "",
    val createdAt: Timestamp = Timestamp.now()
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return sdf.format(createdAt.toDate())
    }

    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm 'WIB'", Locale("id", "ID"))
        return sdf.format(createdAt.toDate())
    }
}