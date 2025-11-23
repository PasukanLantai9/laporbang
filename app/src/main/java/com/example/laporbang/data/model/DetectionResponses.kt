package com.example.laporbang.data.model

data class DetectionResponse(
    val isPotholeDetected: Boolean,
    val confidence: Float,
    val severity: String,
    val description: String
)