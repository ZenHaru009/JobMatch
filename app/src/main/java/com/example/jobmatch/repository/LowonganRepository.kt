package com.example.jobmatch.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.jobmatch.model.Lowongan
import com.example.jobmatch.model.User
import com.example.jobmatch.model.UserRole
import com.example.jobmatch.utils.SawCalculator
import kotlinx.coroutines.tasks.await

class LowonganRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getAllLowongan(): List<Lowongan> {
        return try {
            val snapshot = firestore.collection("lowongan").get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Lowongan::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLowonganByCompanyId(companyId: String): List<Lowongan> {
        return try {
            val snapshot = firestore.collection("lowongan")
                .whereEqualTo("companyId", companyId)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Lowongan::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addLowongan(lowongan: Lowongan): String {
        val docRef = firestore.collection("lowongan").document()
        val withId = lowongan.copy(id = docRef.id)
        docRef.set(withId).await()
        return docRef.id
    }

    suspend fun updateLowongan(lowongan: Lowongan) {
        firestore.collection("lowongan").document(lowongan.id).set(lowongan).await()
    }

    suspend fun deleteLowongan(id: String) {
        firestore.collection("lowongan").document(id).delete().await()
    }

    suspend fun getLowonganById(id: String): Lowongan? {
        val doc = firestore.collection("lowongan").document(id).get().await()
        return doc.toObject(Lowongan::class.java)?.copy(id = doc.id)
    }

    // Rekomendasi SAW
    suspend fun getRecommendedLowongan(userId: String): List<Pair<Lowongan, Double>> {
        val userDoc = firestore.collection("users").document(userId).get().await()
        val jobseeker = userDoc.toObject(User::class.java)
        if (jobseeker == null || jobseeker.role != UserRole.JOBSEEKER) {
            return emptyList()
        }
        val allLowongan = getAllLowongan()
        val scored = allLowongan.mapNotNull { lowongan ->
            val score = SawCalculator.calculateScore(jobseeker, lowongan)
            if (score > 0) lowongan to score else null
        }
        return scored.sortedByDescending { it.second }
    }
}