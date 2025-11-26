package com.example.laporbang.data.model

import com.google.gson.annotations.SerializedName

data class PredictResponse(
    @SerializedName("model")
    val model: String,

    @SerializedName("result")
    val result: PredictResult
)

data class PredictResult(
    @SerializedName("ada_pothole")
    val isPotholeDetected: Boolean,

    @SerializedName("jumlah_pothole")
    val potholeCount: Int,

    @SerializedName("deskripsi")
    val description: String
)