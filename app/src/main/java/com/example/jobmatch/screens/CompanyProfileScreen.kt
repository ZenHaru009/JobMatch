package com.example.jobmatch.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pending
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import com.example.jobmatch.ui.theme.ScrollableScreen
import coil.compose.rememberAsyncImagePainter
import com.example.jobmatch.viewmodel.CompanyProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyProfileScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: CompanyProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val company by viewModel.company.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()

    // State untuk form
    var nama by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var lokasi by remember { mutableStateOf("") }
    var selectedDokumenUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFotoUri by remember { mutableStateOf<Uri?>(null) }
    var fotoBase64FromServer by remember { mutableStateOf<String?>(null) }
    var dokumenError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Launcher untuk memilih dokumen dengan validasi ukuran
    val dokumenPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        dokumenError = null
        uri?.let {
            try {
                val size = context.contentResolver.openAssetFileDescriptor(it, "r")?.use { fd ->
                    fd.length
                } ?: 0
                if (size > 950_000) {
                    dokumenError = "Ukuran file terlalu besar (maks 950KB). Silakan kompres PDF Anda."
                    selectedDokumenUri = null
                } else {
                    selectedDokumenUri = it
                }
            } catch (e: Exception) {
                dokumenError = "Gagal membaca file: ${e.message}"
                selectedDokumenUri = null
            }
        }
    }

    val fotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFotoUri = uri
    }

    LaunchedEffect(Unit) {
        viewModel.loadCompanyProfile()
    }

    LaunchedEffect(company) {
        company?.let {
            nama = it.nama
            deskripsi = it.deskripsi ?: ""
            lokasi = it.lokasi ?: ""
            fotoBase64FromServer = it.fotoBase64
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            kotlinx.coroutines.delay(1000)
            navController.popBackStack()
            viewModel.clearStatus()
        }
    }

    // Konversi Base64 ke Bitmap untuk foto dari server
    val imageBitmap = remember(fotoBase64FromServer) {
        if (!fotoBase64FromServer.isNullOrEmpty()) {
            val bytes = Base64.decode(fotoBase64FromServer, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } else null
    }

    ScrollableScreen(
        title = "Profil Perusahaan",
        onBackClick = { navController.popBackStack() },
        isRefreshing = isLoading,
        onRefresh = { viewModel.loadCompanyProfile() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================== FOTO PROFIL ==================
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(120.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp
                        ) {
                            when {
                                selectedFotoUri != null -> {
                                    Image(
                                        painter = rememberAsyncImagePainter(selectedFotoUri),
                                        contentDescription = "Foto Profil",
                                        modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.extraLarge),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                imageBitmap != null -> {
                                    Image(
                                        bitmap = imageBitmap!!,
                                        contentDescription = "Foto Profil",
                                        modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.extraLarge),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else -> {
                                    Icon(
                                        Icons.Default.Business, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(48.dp).align(Alignment.Center),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        SmallFloatingActionButton(
                            onClick = { fotoPickerLauncher.launch("image/*") },
                            modifier = Modifier.align(Alignment.BottomEnd),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Ubah Foto", modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Logo Perusahaan", style = MaterialTheme.typography.labelLarge)
                }
            }

            // ================== DATA PERUSAHAAN ==================
            OutlinedTextField(
                value = nama,
                onValueChange = { nama = it },
                label = { Text("Nama Perusahaan") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )
            OutlinedTextField(
                value = deskripsi,
                onValueChange = { deskripsi = it },
                label = { Text("Deskripsi") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value = lokasi,
                onValueChange = { lokasi = it },
                label = { Text("Lokasi Kantor Pusat") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
            )

            // Status verifikasi
            company?.verificationStatus?.let { status ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = when (status) {
                            "verified" -> Color(0xFFE8F5E9)
                            "rejected" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when(status) {
                                "verified" -> Icons.Default.CheckCircle
                                "rejected" -> Icons.Default.Error
                                else -> Icons.Default.Pending
                            },
                            contentDescription = null,
                            tint = when(status) {
                                "verified" -> Color(0xFF2E7D32)
                                "rejected" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = when (status) {
                                "pending" -> "Menunggu Verifikasi Admin"
                                "verified" -> "Akun Terverifikasi"
                                "rejected" -> "Verifikasi Ditolak"
                                else -> "Status Tidak Diketahui"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = when(status) {
                                "verified" -> Color(0xFF2E7D32)
                                "rejected" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }
                }
            }

            // Dokumen Verifikasi
            Text(
                "Dokumen Legalitas", 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                "Upload NPWP, NIB, atau Akta Pendirian (PDF, maks 950KB)", 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            FilledTonalButton(
                onClick = { dokumenPickerLauncher.launch("application/pdf") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Description, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (selectedDokumenUri == null) "Pilih File PDF" else "Ganti File PDF")
            }
            
            if (selectedDokumenUri != null) {
                Text("File siap diunggah", color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelSmall)
            }
            
            if (dokumenError != null) {
                Text(dokumenError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Simpan
            Button(
                onClick = {
                    coroutineScope.launch {
                        var dokumenBase64: String? = null
                        var fotoBase64: String? = null
                        if (selectedDokumenUri != null && dokumenError == null) {
                            dokumenBase64 = viewModel.uriToBase64(selectedDokumenUri!!, context)
                        }
                        if (selectedFotoUri != null) {
                            fotoBase64 = viewModel.uriToBase64(selectedFotoUri!!, context)
                        }
                        viewModel.updateCompanyProfile(nama, deskripsi, lokasi, dokumenBase64, fotoBase64)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Update Profil Perusahaan", style = MaterialTheme.typography.titleMedium)
            }

            if (error != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(error!!, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
