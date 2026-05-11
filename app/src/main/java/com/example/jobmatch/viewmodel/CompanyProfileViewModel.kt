package com.example.jobmatch.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.example.jobmatch.model.Perusahaan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CompanyProfileViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _company = MutableStateFlow<Perusahaan?>(null)
    val company: StateFlow<Perusahaan?> = _company.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    fun loadCompanyProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = auth.currentUser?.uid ?: throw Exception("Perusahaan tidak ditemukan")
                val doc = firestore.collection("perusahaan").document(uid).get().await()
                val companyData = doc.toObject(Perusahaan::class.java)?.copy(uid = doc.id)
                _company.value = companyData
                
                // Sinkronisasi status verifikasi ke semua lowongan milik perusahaan ini (Auto-fix)
                if (companyData != null && companyData.verified) {
                    syncVerificationToJobs(uid, true)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun syncVerificationToJobs(companyId: String, isVerified: Boolean) {
        try {
            val jobs = firestore.collection("lowongan")
                .whereEqualTo("companyId", companyId)
                .whereEqualTo("companyVerified", !isVerified) // Cari yang tidak sinkron
                .get()
                .await()
            
            if (!jobs.isEmpty) {
                val batch = firestore.batch()
                for (doc in jobs.documents) {
                    batch.update(doc.reference, "companyVerified", isVerified)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            // Abaikan error sinkronisasi latar belakang
        }
    }

    // Konversi URI file ke Base64
    suspend fun uriToBase64(uri: Uri, context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) return@withContext null
                val bytes = inputStream.readBytes()
                Base64.encodeToString(bytes, Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
        }
    }

    // Simpan data perusahaan (nama, deskripsi, lokasi, dokumen Base64, foto Base64)
    fun updateCompanyProfile(
        nama: String,
        deskripsi: String?,
        lokasi: String?,
        dokumenBase64: String?,
        fotoBase64: String?   // <-- tambahan foto
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _updateSuccess.value = false
            try {
                val uid = auth.currentUser?.uid ?: throw Exception("Perusahaan tidak ditemukan")
                val docRef = firestore.collection("perusahaan").document(uid)
                val updates = mutableMapOf<String, Any>()
                updates["nama"] = nama
                updates["deskripsi"] = deskripsi ?: ""
                updates["lokasi"] = lokasi ?: ""
                if (dokumenBase64 != null) {
                    updates["dokumenBase64"] = dokumenBase64
                }
                if (fotoBase64 != null) {
                    updates["fotoBase64"] = fotoBase64
                }
                // Set default fields jika dokumen baru
                updates["verificationStatus"] = "pending"
                updates["verified"] = false
                updates["email"] = auth.currentUser?.email ?: ""

                // Gunakan set dengan merge
                docRef.set(updates, SetOptions.merge()).await()
                _updateSuccess.value = true
                loadCompanyProfile() // reload data
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearStatus() {
        _updateSuccess.value = false
        _error.value = null
    }
}