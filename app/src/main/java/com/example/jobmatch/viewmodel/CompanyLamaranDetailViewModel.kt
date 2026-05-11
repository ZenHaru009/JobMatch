package com.example.jobmatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.example.jobmatch.model.Lamaran
import com.example.jobmatch.model.LamaranStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CompanyLamaranDetailViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _lamaran = MutableStateFlow<Lamaran?>(null)
    val lamaran: StateFlow<Lamaran?> = _lamaran.asStateFlow()

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
            _lamaran.value = doc.toObject(Lamaran::class.java)?.copy(id = doc.id)
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