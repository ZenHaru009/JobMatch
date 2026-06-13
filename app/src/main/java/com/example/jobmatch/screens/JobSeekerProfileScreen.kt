package com.example.jobmatch.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import com.example.jobmatch.ui.theme.ScrollableScreen
import com.example.jobmatch.viewmodel.JobSeekerProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerProfileScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: JobSeekerProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var name by remember { mutableStateOf("") }
    var pendidikan by remember { mutableStateOf("") }
    var keahlianStr by remember { mutableStateOf("") }
    var pengalamanTahun by remember { mutableStateOf("0") }
    var lokasi by remember { mutableStateOf("") }
    var gajiHarapan by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    // Update state saat profil dimuat dari database (hanya sekali saat inisialisasi)
    LaunchedEffect(profile) {
        if (profile != null && !isInitialized) {
            name = profile!!.name
            pendidikan = profile!!.pendidikan ?: ""
            keahlianStr = profile!!.keahlian?.joinToString(", ") ?: ""
            pengalamanTahun = profile!!.pengalamanTahun?.toString() ?: "0"
            lokasi = profile!!.lokasi ?: ""
            gajiHarapan = profile!!.gajiHarapan?.let { if (it == 0) "" else it.toString() } ?: ""
            isInitialized = true
        }
    }

    val pendidikanOptions = listOf("SMA", "D3", "S1", "S2")
    var expanded by remember { mutableStateOf(false) }

    // Navigasi balik jika berhasil simpan
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            kotlinx.coroutines.delay(500)
            navController.popBackStack()
            viewModel.clearSuccess()
        }
    }

    val isEditMode = profile?.profileCompleted == true

    ScrollableScreen(
        title = if (isEditMode) "Edit Profil" else "Lengkapi Profil",
        onBackClick = { navController.popBackStack() },
        isRefreshing = isLoading,
        onRefresh = { viewModel.loadProfile() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ... (rest of the fields)
            // Nama
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth()
            )

            // Dropdown Pendidikan
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = pendidikan,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pendidikan Terakhir") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    pendidikanOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                pendidikan = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Keahlian
            OutlinedTextField(
                value = keahlianStr,
                onValueChange = { keahlianStr = it },
                label = { Text("Keahlian (pisahkan dengan koma)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Contoh: Kotlin, Firebase, UI/UX") }
            )

            // Pengalaman
            OutlinedTextField(
                value = pengalamanTahun,
                onValueChange = { pengalamanTahun = it },
                label = { Text("Pengalaman (tahun)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Lokasi
            OutlinedTextField(
                value = lokasi,
                onValueChange = { lokasi = it },
                label = { Text("Lokasi Domisili") },
                modifier = Modifier.fillMaxWidth()
            )

            // Gaji yang Diharapkan
            OutlinedTextField(
                value = gajiHarapan,
                onValueChange = { gajiHarapan = it },
                label = { Text("Gaji yang Diharapkan (contoh: 8000000)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Tombol Simpan
            Button(
                onClick = {
                    val keahlianList = keahlianStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val tahun = pengalamanTahun.filter { it.isDigit() }.toIntOrNull() ?: 0
                    val gaji = gajiHarapan.filter { it.isDigit() }.toIntOrNull() ?: 0
                    viewModel.updateProfile(name, pendidikan, keahlianList, tahun, lokasi, gaji)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text(if (isEditMode) "Update Profil" else "Simpan Profil")
            }

            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            if (saveSuccess) Text("Profil berhasil disimpan!", color = MaterialTheme.colorScheme.primary)
        }
    }
}