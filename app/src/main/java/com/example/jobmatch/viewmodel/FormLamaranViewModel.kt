package com.example.jobmatch.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.jobmatch.model.Lamaran
import com.example.jobmatch.model.LamaranStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream

class FormLamaranViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success.asStateFlow()

    suspend fun uriToBase64(uri: Uri, context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream == null) return@withContext null
                val bytes = inputStream.readBytes()
                Base64.encodeToString(bytes, Base64.DEFAULT)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun submitLamaran(
        lowonganId: String,
        userName: String,
        cvBase64: String?,
        noHp: String?   // <-- TAMBAHAN
    ): Boolean {
        _isLoading.value = true
        _error.value = null
        _success.value = false
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val existing = firestore.collection("lamaran")
                .whereEqualTo("lowonganId", lowonganId)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            if (!existing.isEmpty) {
                _error.value = "Anda sudah melamar ke lowongan ini"
                return false
            }
            val lamaran = Lamaran(
                lowonganId = lowonganId,
                userId = userId,
                userName = userName,
                status = LamaranStatus.DIPROSES,
                tanggalLamaran = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                cvBase64 = cvBase64,
                noHp = noHp                // <-- TAMBAHAN
            )
            firestore.collection("lamaran").add(lamaran).await()
            _success.value = true
            true
        } catch (e: Exception) {
            _error.value = e.message
            false
        } finally {
            _isLoading.value = false
        }
    }

    fun clearStatus() {
        _success.value = false
        _error.value = null
    }
}