package com.example.jobmatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobmatch.model.UserRole
import com.example.jobmatch.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepo = AuthRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _resetSuccess = MutableStateFlow(false)
    val resetSuccess: StateFlow<Boolean> = _resetSuccess.asStateFlow()

    private val _loginSuccess = MutableStateFlow<Pair<UserRole, String>?>(null)
    val loginSuccess: StateFlow<Pair<UserRole, String>?> = _loginSuccess.asStateFlow()

    val currentUser = authRepo.currentUser

    fun register(email: String, password: String, name: String, role: UserRole) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepo.register(email, password, name, role)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            } else {
                _error.value = null
            }
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginSuccess.value = null
            val result = authRepo.login(email, password)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
                _isLoading.value = false
            } else {
                // Login sukses, ambil data user dari Firestore
                try {
                    val user = authRepo.refreshCurrentUser() // <-- sekarang mengembalikan User?
                    if (user != null) {
                        _loginSuccess.value = Pair(user.role, user.uid)
                        _error.value = null
                    } else {
                        _error.value = "Gagal mengambil data pengguna"
                    }
                } catch (e: Exception) {
                    _error.value = e.message
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepo.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                _resetSuccess.value = true
                _error.value = null
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Gagal mengirim email reset"
                _resetSuccess.value = false
            }
            _isLoading.value = false
        }
    }

    fun clearLoginSuccess() {
        _loginSuccess.value = null
    }

    fun clearResetSuccess() {
        _resetSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }

    fun logout() {
        authRepo.logout()
        _loginSuccess.value = null
    }
}