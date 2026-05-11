package com.example.jobmatch.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.jobmatch.model.Perusahaan
import kotlinx.coroutines.tasks.await

class CompanyRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Fungsi lama (masih dipertahankan untuk kompatibilitas)
    suspend fun getUnverifiedCompanies(): List<Perusahaan> {
        // Mengambil perusahaan dengan verificationStatus = "pending" (atau verified = false jika field lama ada)
        return try {
            val snapshot1 = firestore.collection("perusahaan")
                .whereEqualTo("verificationStatus", "pending")
                .get()
                .await()
            if (snapshot1.isEmpty) {
                // Fallback untuk data lama yang belum punya verificationStatus
                val snapshot2 = firestore.collection("perusahaan")
                    .whereEqualTo("verified", false)
                    .get()
                    .await()
                snapshot2.documents.mapNotNull { doc ->
                    doc.toObject(Perusahaan::class.java)?.copy(uid = doc.id)
                }
            } else {
                snapshot1.documents.mapNotNull { doc ->
                    doc.toObject(Perusahaan::class.java)?.copy(uid = doc.id)
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Fungsi baru: mengambil perusahaan berdasarkan status verifikasi
    suspend fun getCompaniesByVerificationStatus(status: String): List<Perusahaan> {
        return try {
            val snapshot = firestore.collection("perusahaan")
                .whereEqualTo("verificationStatus", status)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Perusahaan::class.java)?.copy(uid = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Fungsi update status baru (mengubah verificationStatus dan field verified untuk kompatibilitas)
    suspend fun updateVerificationStatus(companyId: String, newStatus: String) {
        val isVerified = newStatus == "verified"
        val updates = mapOf(
            "verificationStatus" to newStatus,
            "verified" to isVerified
        )
        // Update dokumen perusahaan
        firestore.collection("perusahaan").document(companyId).update(updates).await()

        // Update semua lowongan milik perusahaan ini agar status verifikasinya sinkron
        try {
            val lowonganSnapshot = firestore.collection("lowongan")
                .whereEqualTo("companyId", companyId)
                .get()
                .await()
            
            if (!lowonganSnapshot.isEmpty) {
                val batch = firestore.batch()
                for (doc in lowonganSnapshot.documents) {
                    batch.update(doc.reference, "companyVerified", isVerified)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            // Log error atau abaikan jika gagal update lowongan (perusahaan tetap terupdate)
            e.printStackTrace()
        }
    }

    // Fungsi lama verifyCompany (dipertahankan agar tidak merusak kode lain)
    @Deprecated("Gunakan updateVerificationStatus dengan status string", ReplaceWith("updateVerificationStatus(uid, if (verified) \"verified\" else \"rejected\")"))
    suspend fun verifyCompany(uid: String, verified: Boolean) {
        val newStatus = if (verified) "verified" else "rejected"
        updateVerificationStatus(uid, newStatus)
    }
}