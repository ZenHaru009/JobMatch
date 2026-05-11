package com.example.jobmatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.jobmatch.model.Lowongan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CompanyLowonganListViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _lowonganList = MutableStateFlow<List<Lowongan>>(emptyList())
    val lowonganList: StateFlow<List<Lowongan>> = _lowonganList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadCompanyLowongan() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val companyId = auth.currentUser?.uid ?: throw Exception("Perusahaan tidak ditemukan")
                val snapshot = firestore.collection("lowongan")
                    .whereEqualTo("companyId", companyId)
                    .get()
                    .await()
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Lowongan::class.java)?.copy(id = doc.id)
                }
                _lowonganList.value = list
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteLowongan(lowonganId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("lowongan").document(lowonganId).delete().await()
                // setelah hapus, refresh daftar
                loadCompanyLowongan()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() { _error.value = null }
}