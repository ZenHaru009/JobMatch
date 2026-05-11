package com.example.jobmatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.jobmatch.model.Lamaran
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TrackingViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _lamaranList = MutableStateFlow<List<Lamaran>>(emptyList())
    val lamaranList: StateFlow<List<Lamaran>> = _lamaranList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadMyLamaran() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
                // Hapus orderBy untuk menghindari kebutuhan indeks
                val snapshot = firestore.collection("lamaran")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Lamaran::class.java)?.copy(id = doc.id)
                }
                // Urutkan manual secara descending berdasarkan tanggal lamaran
                _lamaranList.value = list.sortedByDescending { it.tanggalLamaran }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
}