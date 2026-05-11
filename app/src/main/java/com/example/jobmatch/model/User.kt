package com.example.jobmatch.model

enum class UserRole {
    JOBSEEKER, COMPANY, ADMIN
}

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.JOBSEEKER,
    val profileCompleted: Boolean = false,
    // Untuk jobseeker (digunakan dalam perhitungan SAW)
    val pendidikan: String? = null,
    val keahlian: List<String>? = null,
    val pengalamanTahun: Int? = null,
    val lokasi: String? = null,
    val gajiHarapan: Int? = null   // <-- TAMBAHAN
)