package com.example.jobmatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobmatch.model.Lowongan
import com.example.jobmatch.repository.LowonganRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LowonganViewModel : ViewModel() {
    private val repository = LowonganRepository()

    private val _lowonganList = MutableStateFlow<List<Lowongan>>(emptyList())
    val lowonganList: StateFlow<List<Lowongan>> = _lowonganList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ========== TAMBAHAN YANG DIMINTA ==========
    private val _currentLowongan = MutableStateFlow<Lowongan?>(null)
    val currentLowongan: StateFlow<Lowongan?> = _currentLowongan.asStateFlow()

    fun loadLowonganById(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val lowongan = repository.getLowonganById(id)
                _currentLowongan.value = lowongan
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCurrentLowongan() {
        _currentLowongan.value = null
    }
    // ========== AKHIR TAMBAHAN ==========

    fun loadAllLowongan() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.getAllLowongan()
                _lowonganList.value = list
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getLowonganById(lowonganId: String): Lowongan? {
        return _lowonganList.value.find { it.id == lowonganId }
    }

    fun addLowongan(lowongan: Lowongan) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.addLowongan(lowongan)
                loadAllLowongan()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateLowongan(lowongan: Lowongan) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateLowongan(lowongan)
                loadAllLowongan()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteLowongan(lowonganId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteLowongan(lowonganId)
                loadAllLowongan()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}