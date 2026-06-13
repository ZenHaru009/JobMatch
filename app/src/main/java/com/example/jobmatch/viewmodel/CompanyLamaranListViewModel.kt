package com.example.jobmatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.jobmatch.model.Lamaran
import com.example.jobmatch.model.LamaranStatus
import com.example.jobmatch.model.Lowongan
import com.example.jobmatch.model.User
import com.example.jobmatch.utils.SawCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CompanyLamaranListViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val MATCH_THRESHOLD = 0.8 // Ambang batas 80% untuk otomatis Interview

    data class LamaranWithLowongan(
        val lamaran: Lamaran,
        val lowonganJudul: String,
        val lowonganLokasi: String,
        val matchScore: Double = 0.0
    )

    private val _lamaranList = MutableStateFlow<List<LamaranWithLowongan>>(emptyList())
    val lamaranList: StateFlow<List<LamaranWithLowongan>> = _lamaranList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadLamaranForCompany() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val companyId = auth.currentUser?.uid ?: throw Exception("Perusahaan tidak ditemukan")
                val lowonganSnapshot = firestore.collection("lowongan")
                    .whereEqualTo("companyId", companyId)
                    .get()
                    .await()
                val lowonganMap = lowonganSnapshot.documents.associate { doc ->
                    doc.id to (doc.toObject(Lowongan::class.java) ?: Lowongan())
                }
                if (lowonganMap.isEmpty()) {
                    _lamaranList.value = emptyList()
                    return@launch
                }
                val lamaranSnapshot = firestore.collection("lamaran")
                    .whereIn("lowonganId", lowonganMap.keys.toList())
                    .get()
                    .await()
                
                val userIds = lamaranSnapshot.documents.mapNotNull { it.getString("userId") }.distinct()
                val userMap = mutableMapOf<String, User>()
                if (userIds.isNotEmpty()) {
                    // Firestore whereIn limit is 10, but for simplicity we assume it might be more or we can chunk it.
                    // For now, let's fetch them one by one or in chunks if needed. 
                    // To keep it simple and robust, let's fetch each user.
                    userIds.forEach { uid ->
                        val userDoc = firestore.collection("users").document(uid).get().await()
                        userDoc.toObject(User::class.java)?.let { userMap[uid] = it }
                    }
                }

                val list = lamaranSnapshot.documents.mapNotNull { doc ->
                    var lamaran = doc.toObject(Lamaran::class.java)?.copy(id = doc.id) ?: return@mapNotNull null
                    val lowongan = lowonganMap[lamaran.lowonganId]
                    val user = userMap[lamaran.userId]
                    
                    if (lowongan != null) {
                        val score = if (user != null) SawCalculator.calculateScore(user, lowongan) else 0.0
                        
                        // OTOMATIS STATUS JADI INTERVIEW KALO SKOR TINGGI (>= 80%)
                        if (score >= MATCH_THRESHOLD && lamaran.status == LamaranStatus.DIPROSES) {
                            // Update di database (Firestore)
                            updateLamaranStatusToInterview(lamaran.id)
                            // Update object lokal agar UI langsung berubah
                            lamaran = lamaran.copy(status = LamaranStatus.INTERVIEW)
                        }
                        
                        LamaranWithLowongan(lamaran, lowongan.judul, lowongan.lokasi, score)
                    } else null
                }.sortedByDescending { it.matchScore }
                _lamaranList.value = list
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateLamaranStatusToInterview(lamaranId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("lamaran").document(lamaranId)
                    .update("status", LamaranStatus.INTERVIEW.name, "updatedAt", System.currentTimeMillis())
                    .await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
