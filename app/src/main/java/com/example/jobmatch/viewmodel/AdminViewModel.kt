package com.example.jobmatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobmatch.model.Perusahaan
import com.example.jobmatch.repository.CompanyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val repository = CompanyRepository()

    private val _companies = MutableStateFlow<List<Perusahaan>>(emptyList())
    val companies: StateFlow<List<Perusahaan>> = _companies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Memuat perusahaan dengan status verifikasi "pending"
    fun loadPendingCompanies() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.getCompaniesByVerificationStatus("pending")
                _companies.value = list
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Memperbarui status verifikasi perusahaan (pending, verified, rejected)
    fun updateVerificationStatus(companyId: String, newStatus: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateVerificationStatus(companyId, newStatus)
                loadPendingCompanies() // refresh daftar
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Opsional: untuk kompatibilitas dengan kode lama yang masih menggunakan verifyCompany dengan Boolean
    @Deprecated("Gunakan updateVerificationStatus dengan status string", ReplaceWith("updateVerificationStatus(uid, if (verified) \"verified\" else \"rejected\")"))
    fun verifyCompany(uid: String, verified: Boolean) {
        val newStatus = if (verified) "verified" else "rejected"
        updateVerificationStatus(uid, newStatus)
    }

    fun clearError() {
        _error.value = null
    }
}