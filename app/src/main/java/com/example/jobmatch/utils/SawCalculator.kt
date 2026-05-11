package com.example.jobmatch.utils

import com.example.jobmatch.model.Lowongan
import com.example.jobmatch.model.User

object SawCalculator {

    // Bobot kriteria (total = 1.0)
    private val weights = mapOf(
        "pendidikan" to 0.30,
        "keahlian" to 0.35,
        "pengalaman" to 0.20,
        "lokasi" to 0.10,
        "gaji" to 0.05
    )

    fun calculateScore(jobseeker: User, lowongan: Lowongan): Double {
        val normPendidikan = normalizePendidikan(jobseeker.pendidikan, lowongan.pendidikanMin)
        val normKeahlian = normalizeKeahlian(jobseeker.keahlian, lowongan.keahlianDibutuhkan)
        val normPengalaman = normalizePengalaman(jobseeker.pengalamanTahun ?: 0, lowongan.pengalamanMin)
        val normLokasi = normalizeLokasi(jobseeker.lokasi, lowongan.lokasi)
        val normGaji = normalizeGaji(jobseeker.gajiHarapan ?: 0, lowongan.gaji)

        return (normPendidikan * weights["pendidikan"]!!) +
                (normKeahlian * weights["keahlian"]!!) +
                (normPengalaman * weights["pengalaman"]!!) +
                (normLokasi * weights["lokasi"]!!) +
                (normGaji * weights["gaji"]!!)
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
        if (jobLoc.isNullOrBlank()) return 0.5
        return if (jobLoc.equals(lowonganLoc, ignoreCase = true)) 1.0 else 0.5
    }

    private fun normalizeGaji(jobGaji: Int, lowonganGajiStr: String): Double {
        val cleaned = lowonganGajiStr.replace("Rp", "").replace(".", "").trim()
        val parts = cleaned.split("-").map { it.trim() }
        val minGaji = parts.firstOrNull()?.toIntOrNull() ?: 0
        val maxGaji = parts.getOrNull(1)?.toIntOrNull() ?: minGaji
        return when {
            jobGaji <= 0 -> 0.5
            jobGaji in minGaji..maxGaji -> 1.0
            jobGaji < minGaji -> jobGaji.toDouble() / minGaji
            else -> maxGaji.toDouble() / jobGaji
        }.coerceIn(0.0, 1.0)
    }
}