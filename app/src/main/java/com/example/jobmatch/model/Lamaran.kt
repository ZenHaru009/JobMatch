package com.example.jobmatch.model

enum class LamaranStatus {
    DIPROSES, INTERVIEW, DITERIMA, DITOLAK
}

data class Lamaran(
    val id: String = "",
    val lowonganId: String = "",
    val userId: String = "",
    val userName: String = "",
    val status: LamaranStatus = LamaranStatus.DIPROSES,
    val tanggalLamaran: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val cvBase64: String? = null,
    val noHp: String? = null   // <-- Sekarang sudah benar dengan koma
)