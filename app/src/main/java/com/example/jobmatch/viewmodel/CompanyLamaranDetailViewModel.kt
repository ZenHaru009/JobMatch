package com.example.jobmatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class CompanyLamaranDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _lamaran = MutableStateFlow<Lamaran?>(null)
    val lamaran: StateFlow<Lamaran?> = _lamaran.asStateFlow()

    private val _matchScore = MutableStateFlow(0.0)
    val matchScore: StateFlow<Double> = _matchScore.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    suspend fun loadLamaran(lamaranId: String) {
        _isLoading.value = true
        try {
            val doc = firestore.collection("lamaran").document(lamaranId).get().await()
            val lamaranObj = doc.toObject(Lamaran::class.java)?.copy(id = doc.id)
            _lamaran.value = lamaranObj
            
            if (lamaranObj != null) {
                // Fetch user and lowongan to calculate SAW score
                val userDoc = firestore.collection("users").document(lamaranObj.userId).get().await()
                val user = userDoc.toObject(User::class.java)
                
                val lowonganDoc = firestore.collection("lowongan").document(lamaranObj.lowonganId).get().await()
                val lowongan = lowonganDoc.toObject(Lowongan::class.java)
                
                if (user != null && lowongan != null) {
                    _matchScore.value = SawCalculator.calculateScore(user, lowongan)
                }
            }
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    fun updateStatusLamaran(lamaranId: String, newStatus: LamaranStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            _updateSuccess.value = false
            try {
                firestore.collection("lamaran").document(lamaranId)
                    .update("status", newStatus.name, "updatedAt", System.currentTimeMillis())
                    .await()
                _updateSuccess.value = true
                loadLamaran(lamaranId) // reload
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}