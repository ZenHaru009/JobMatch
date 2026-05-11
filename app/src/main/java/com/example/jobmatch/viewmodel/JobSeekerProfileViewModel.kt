package com.example.jobmatch.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.example.jobmatch.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class JobSeekerProfileViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "JobSeekerProfileVM"

    private val _profile = MutableStateFlow<User?>(null)
    val profile: StateFlow<User?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
                val doc = firestore.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    _profile.value = doc.toObject(User::class.java)
                } else {
                    // Jika dokumen belum ada, buat baru dengan default
                    val newUser = User(uid = uid, email = auth.currentUser?.email ?: "")
                    firestore.collection("users").document(uid).set(newUser).await()
                    _profile.value = newUser
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadProfile error: ${e.message}")
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(
        name: String,
        pendidikan: String,
        keahlian: List<String>,
        pengalamanTahun: Int,
        lokasi: String,
        gajiHarapan: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _saveSuccess.value = false
            _error.value = null
            try {
                val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
                val updates = mapOf(
                    "name" to name,
                    "pendidikan" to pendidikan,
                    "keahlian" to keahlian,
                    "pengalamanTahun" to pengalamanTahun,
                    "lokasi" to lokasi,
                    "gajiHarapan" to gajiHarapan,
                    "profileCompleted" to true
                )
                // Gunakan set dengan merge agar aman jika dokumen belum ada
                firestore.collection("users").document(uid)
                    .set(updates, SetOptions.merge())
                    .await()
                Log.d(TAG, "Profile updated successfully for user $uid")
                _saveSuccess.value = true
                loadProfile() // reload
            } catch (e: Exception) {
                Log.e(TAG, "updateProfile error: ${e.message}")
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
    fun clearSuccess() { _saveSuccess.value = false }
}