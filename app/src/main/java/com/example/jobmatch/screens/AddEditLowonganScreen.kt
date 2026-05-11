package com.example.jobmatch.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import com.example.jobmatch.model.Lowongan
import com.example.jobmatch.ui.theme.ScrollableScreen
import com.example.jobmatch.viewmodel.CompanyProfileViewModel
import com.example.jobmatch.viewmodel.LowonganViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLowonganScreen(
    navController: NavController,
    lowonganId: String,  // "new" jika tambah, atau ID lowongan jika edit
    windowSizeClass: WindowSizeClass,
    viewModel: LowonganViewModel = viewModel(),
    companyViewModel: CompanyProfileViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val currentLowongan by viewModel.currentLowongan.collectAsState()
    val company by companyViewModel.company.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Form state
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var persyaratan by remember { mutableStateOf("") }
    var lokasi by remember { mutableStateOf("") }
    var gaji by remember { mutableStateOf("") }
    var pendidikanMin by remember { mutableStateOf("") }
    var keahlianStr by remember { mutableStateOf("") }
    var pengalamanMin by remember { mutableStateOf("0") }

    // Load data perusahaan untuk ambil status verifikasi
    LaunchedEffect(Unit) {
        companyViewModel.loadCompanyProfile()
    }

    // Load data jika mode edit
    LaunchedEffect(lowonganId, currentLowongan) {
        if (lowonganId != "new" && currentLowongan == null) {
            viewModel.loadLowonganById(lowonganId)
        } else if (currentLowongan != null && currentLowongan?.id == lowonganId) {
            judul = currentLowongan!!.judul
            deskripsi = currentLowongan!!.deskripsi
            persyaratan = currentLowongan!!.persyaratan
            lokasi = currentLowongan!!.lokasi
            gaji = currentLowongan!!.gaji
            pendidikanMin = currentLowongan!!.pendidikanMin
            keahlianStr = currentLowongan!!.keahlianDibutuhkan.joinToString(",")
            pengalamanMin = currentLowongan!!.pengalamanMin.toString()
        }
    }

    // Fungsi simpan
    fun saveLowongan() {
        if (judul.isBlank() || deskripsi.isBlank() || lokasi.isBlank() || gaji.isBlank()) {
            // Tampilkan pesan error sederhana (bisa pakai Snackbar, untuk sederhana gunakan error state)
            return
        }
        coroutineScope.launch {
            val companyId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val companyName = company?.nama ?: FirebaseAuth.getInstance().currentUser?.email ?: "Perusahaan"
            val isVerified = company?.verified ?: false
            
            val keahlianList = keahlianStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val lowongan = Lowongan(
                id = if (lowonganId != "new") lowonganId else "",
                companyId = companyId,
                companyName = companyName,
                judul = judul,
                deskripsi = deskripsi,
                persyaratan = persyaratan,
                lokasi = lokasi,
                gaji = gaji,
                pendidikanMin = pendidikanMin,
                keahlianDibutuhkan = keahlianList,
                pengalamanMin = pengalamanMin.toIntOrNull() ?: 0,
                companyVerified = isVerified,
                createdAt = if (lowonganId != "new") currentLowongan?.createdAt ?: System.currentTimeMillis()
                else System.currentTimeMillis()
            )
            if (lowonganId == "new") {
                viewModel.addLowongan(lowongan)
            } else {
                viewModel.updateLowongan(lowongan)
            }
            // Tunggu sebentar lalu kembali
            kotlinx.coroutines.delay(1000)
            navController.popBackStack()
        }
    }

    ScrollableScreen(
        title = if (lowonganId == "new") "Tambah Lowongan" else "Edit Lowongan",
        showBackButton = true,
        onBackClick = { navController.popBackStack() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Detail Informasi Lowongan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = judul,
                onValueChange = { judul = it },
                label = { Text("Judul Lowongan*") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                singleLine = true
            )
            OutlinedTextField(
                value = deskripsi,
                onValueChange = { deskripsi = it },
                label = { Text("Deskripsi Pekerjaan*") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = MaterialTheme.shapes.medium,
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
            )
            OutlinedTextField(
                value = persyaratan,
                onValueChange = { persyaratan = it },
                label = { Text("Persyaratan Tambahan") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = MaterialTheme.shapes.medium,
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
            )
            OutlinedTextField(
                value = lokasi,
                onValueChange = { lokasi = it },
                label = { Text("Lokasi Penempatan*") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                singleLine = true
            )
            OutlinedTextField(
                value = gaji,
                onValueChange = { gaji = it },
                label = { Text("Gaji / Range Gaji*") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                singleLine = true
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Kualifikasi Minimal",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = pendidikanMin,
                onValueChange = { pendidikanMin = it },
                label = { Text("Pendidikan Minimal (SMA/D3/S1/S2)") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                singleLine = true
            )
            OutlinedTextField(
                value = keahlianStr,
                onValueChange = { keahlianStr = it },
                label = { Text("Keahlian Utama (pisahkan koma)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Contoh: Kotlin, Firebase, SQL") },
                shape = MaterialTheme.shapes.medium,
                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) }
            )
            OutlinedTextField(
                value = pengalamanMin,
                onValueChange = { pengalamanMin = it },
                label = { Text("Pengalaman Minimal (tahun)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.medium,
                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { saveLowongan() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text(if (lowonganId == "new") "Publikasikan Lowongan" else "Update Lowongan", style = MaterialTheme.typography.titleMedium)
            }
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}