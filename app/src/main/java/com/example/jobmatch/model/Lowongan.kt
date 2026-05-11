package com.example.jobmatch.model

data class Lowongan(
    val id: String = "",
    val companyId: String = "",
    val companyName: String = "",
    val judul: String = "",
    val deskripsi: String = "",
    val persyaratan: String = "",
    val lokasi: String = "",
    val gaji: String = "",
    val pendidikanMin: String = "",
    val keahlianDibutuhkan: List<String> = emptyList(),
    val pengalamanMin: Int = 0,
    val companyVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)