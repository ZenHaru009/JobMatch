package com.example.jobmatch.model

data class Perusahaan(
    val uid: String = "",
    val nama: String = "",
    val email: String = "",
    val deskripsi: String? = null,
    val lokasi: String? = null,
    val verified: Boolean = false,
    val verificationStatus: String = "pending",
    val dokumenBase64: String? = null,
    val fotoBase64: String? = null   // Tambahkan baris ini
)