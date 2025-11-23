package com.example.laporbang.data.repository

import android.util.Log
import com.example.laporbang.data.model.DetectionResponse
import com.example.laporbang.data.model.Report
import com.example.laporbang.data.model.ReportStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReportRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val reportsCollection = firestore.collection("reports")
    private val usersCollection = firestore.collection("users")

    suspend fun getUserRole(): String {
        return try {
            val uid = auth.currentUser?.uid ?: return "user"
            val snapshot = usersCollection.document(uid).get().await()
            snapshot.getString("role") ?: "user"
        } catch (e: Exception) {
            "user"
        }
    }

    suspend fun updateReportStatus(reportId: String, newStatus: ReportStatus): Result<Boolean> {
        return try {
            reportsCollection.document(reportId)
                .update("status", newStatus)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReport(reportId: String): Result<Boolean> {
        return try {
            reportsCollection.document(reportId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getReports(): Flow<List<Report>> = callbackFlow {
        val listener = reportsCollection.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) {
                    val reports = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Report::class.java)?.copy(id = doc.id)
                    }
                    trySend(reports)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getReportById(reportId: String): Result<Report> {
        return try {
            val doc = reportsCollection.document(reportId).get().await()
            val report = doc.toObject(Report::class.java)?.copy(id = doc.id)
            if (report != null) Result.success(report) else Result.failure(Exception("Not found"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun addReport(report: Report): Result<Boolean> {
        return try {
            reportsCollection.add(report).await()
            Result.success(true)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun detectDamage(imagePath: String): Result<DetectionResponse> {
        delay(2000)
        return Result.success(DetectionResponse(true, 0.98f, "Berat", "Lubang berbahaya."))
    }
}