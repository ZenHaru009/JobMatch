package com.example.jobmatch.utils

import com.example.jobmatch.model.Lowongan
import com.example.jobmatch.model.User

object SawCalculator {

    // Bobot kriteria (Total akumulatif wajib = 1.0)
    private val weights = mapOf(
        "pendidikan" to 0.30,
        "keahlian" to 0.35,
        "pengalaman" to 0.20,
        "lokasi" to 0.10,
        "gaji" to 0.05
    )

    fun calculateScore(jobseeker: User, lowongan: Lowongan): Double {
        val normPendidikan = normalizePendidikan(jobseeker.pendidikan, lowongan.pendidikanMin ?: "SMA")
        val normKeahlian = normalizeKeahlian(jobseeker.keahlian, lowongan.keahlianDibutuhkan ?: emptyList())
        val normPengalaman = normalizePengalaman(jobseeker.pengalamanTahun ?: 0, lowongan.pengalamanMin ?: 0)
        val normLokasi = normalizeLokasi(jobseeker.lokasi, lowongan.lokasi ?: "")
        val normGaji = normalizeGaji(jobseeker.gajiHarapan ?: 0, lowongan.gaji ?: "0")

        // Perkalian matriks ternormalisasi dengan bobot kriteria (Rumus SAW)
        return (normPendidikan * (weights["pendidikan"] ?: 0.30)) +
                (normKeahlian * (weights["keahlian"] ?: 0.35)) +
                (normPengalaman * (weights["pengalaman"] ?: 0.20)) +
                (normLokasi * (weights["lokasi"] ?: 0.10)) +
                (normGaji * (weights["gaji"] ?: 0.05))
    }

    private fun normalizePendidikan(jobPend: String?, lowonganMin: String): Double {
        val levelMap = mapOf("SMA" to 1, "D3" to 2, "S1" to 3, "S2" to 4)
        val jobLevel = levelMap[jobPend] ?: 1
        val minLevel = levelMap[lowonganMin] ?: 1
        return if (jobLevel >= minLevel) 1.0 else jobLevel.toDouble() / minLevel
    }

    private fun normalizeKeahlian(jobSkills: List<String>?, requiredSkills: List<String>): Double {
        if (requiredSkills.isEmpty()) return 1.0
        if (jobSkills.isNullOrEmpty()) return 0.0
        val matched = requiredSkills.count { required ->
            jobSkills.any { it.equals(required, ignoreCase = true) }
        }
        return matched.toDouble() / requiredSkills.size
    }

    private fun normalizePengalaman(jobExp: Int, requiredExp: Int): Double {
        if (requiredExp <= 0) return 1.0
        return if (jobExp >= requiredExp) 1.0 else jobExp.toDouble() / requiredExp
    }

    private fun normalizeLokasi(jobLoc: String?, lowonganLoc: String): Double {
        if (jobLoc.isNullOrBlank() || lowonganLoc.isBlank()) return 0.5
        return if (jobLoc.equals(lowonganLoc, ignoreCase = true)) 1.0 else 0.5
    }

    private fun normalizeGaji(jobGaji: Int, lowonganGajiStr: String): Double {
        if (lowonganGajiStr.isBlank() || lowonganGajiStr == "0") return 0.5

        // Pembersihan string gaji yang lebih aman menggunakan regex
        val cleaned = lowonganGajiStr.replace("Rp", "").replace(".", "").trim()

        // Mendukung pemisah tanda hubung '-' atau kata 'sampai'
        val parts = cleaned.split(Regex("[-–]|sampai|s/d")).map { it.trim() }

        val minGaji = parts.firstOrNull()?.toIntOrNull() ?: 0
        var maxGaji = parts.getOrNull(1)?.toIntOrNull() ?: minGaji

        // Antisipasi jika maxGaji gagal ter-parsing agar tidak bernilai 0
        if (maxGaji <= 0) maxGaji = minGaji
        if (maxGaji <= 0) return 0.5

        return when {
            jobGaji <= 0 -> 0.5
            jobGaji in minGaji..maxGaji -> 1.0
            jobGaji < minGaji -> jobGaji.toDouble() / minGaji
            else -> maxGaji.toDouble() / jobGaji // Pendekatan kriteria cost jika ekspektasi pelamar kegedean
        }.coerceIn(0.0, 1.0)
    }
}