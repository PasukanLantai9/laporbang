package com.example.laporbang.data.repository

import android.util.Log
import com.example.laporbang.data.model.Report
import com.example.laporbang.data.model.ReportStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReportRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val reportsCollection = firestore.collection("reports")

    fun getReports(): Flow<List<Report>> = callbackFlow {
        val listener = reportsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ReportRepo", "Error fetching reports", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val reports = snapshot.documents.mapNotNull { doc ->
                        try {
                            val report = doc.toObject(Report::class.java)
                            report?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(reports)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getReportById(reportId: String): Result<Report> {
        return try {
            val document = reportsCollection.document(reportId).get().await()
            val report = document.toObject(Report::class.java)
            if (report != null) {
                Result.success(report.copy(id = document.id))
            } else {
                Result.failure(Exception("Laporan tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addReport(report: Report): Result<Boolean> {
        return try {
            reportsCollection.add(report).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}