package com.example.laporbang.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint // Pastikan import ini ada
import java.text.SimpleDateFormat
import java.util.Locale

enum class ReportStatus {
    BELUM_DITANGANI,
    DALAM_PROSES,
    SELESAI;

    companion object {
        fun fromString(value: String): ReportStatus {
            return when(value.uppercase()) {
                "BELUM_DITANGANI" -> BELUM_DITANGANI
                "DALAM_PROSES" -> DALAM_PROSES
                "SELESAI" -> SELESAI
                else -> BELUM_DITANGANI // Default
            }
        }
    }
}

data class Report(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val address: String = "",

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val location: GeoPoint = GeoPoint(0.0, 0.0),

    val status: ReportStatus = ReportStatus.BELUM_DITANGANI,
    val imageUrl: String = "",
    val reporterName: String = "",
    val createdAt: Timestamp = Timestamp.now()
) {
    fun getLat(): Double {
        return if (latitude != 0.0) latitude else location.latitude
    }

    fun getLng(): Double {
        return if (longitude != 0.0) longitude else location.longitude
    }

    fun getFormattedDate(): String {
        val date = createdAt.toDate()
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return sdf.format(date)
    }

    fun getFormattedTime(): String {
        val date = createdAt.toDate()
        val sdf = SimpleDateFormat("HH:mm 'WIB'", Locale("id", "ID"))
        return sdf.format(date)
    }
}