package com.example.jobmatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.jobmatch.model.Lamaran
import com.example.jobmatch.model.Lowongan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CompanyLamaranListViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    data class LamaranWithLowongan(
        val lamaran: Lamaran,
        val lowonganJudul: String,
        val lowonganLokasi: String
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
                val list = lamaranSnapshot.documents.mapNotNull { doc ->
                    val lamaran = doc.toObject(Lamaran::class.java)?.copy(id = doc.id) ?: return@mapNotNull null
                    val lowongan = lowonganMap[lamaran.lowonganId]
                    if (lowongan != null) {
                        LamaranWithLowongan(lamaran, lowongan.judul, lowongan.lokasi)
                    } else null
                }.sortedByDescending { it.lamaran.tanggalLamaran }
                _lamaranList.value = list
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}