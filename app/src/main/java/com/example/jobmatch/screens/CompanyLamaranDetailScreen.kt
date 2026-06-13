package com.example.jobmatch.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import com.example.jobmatch.model.LamaranStatus
import com.example.jobmatch.viewmodel.CompanyLamaranDetailViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyLamaranDetailScreen(
    navController: NavController,
    lamaranId: String,
    windowSizeClass: WindowSizeClass,
    viewModel: CompanyLamaranDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val lamaran by viewModel.lamaran.collectAsState()
    val matchScore by viewModel.matchScore.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()

    var selectedStatus by remember { mutableStateOf(lamaran?.status ?: LamaranStatus.DIPROSES) }

    LaunchedEffect(lamaranId) {
        viewModel.loadLamaran(lamaranId)
    }

    LaunchedEffect(lamaran) {
        selectedStatus = lamaran?.status ?: LamaranStatus.DIPROSES
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detail Lamaran", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val coroutineScope = rememberCoroutineScope()
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { coroutineScope.launch { viewModel.loadLamaran(lamaranId) } },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                color = MaterialTheme.colorScheme.background
            ) {
                if (error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: $error", color = MaterialTheme.colorScheme.error)
                    }
                } else if (lamaran == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Lamaran tidak ditemukan")
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Nama Pelamar: ${lamaran!!.userName}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = "${(matchScore * 100).toInt()}% Match",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                                Text("Nomor HP: ${lamaran!!.noHp ?: "Tidak diisi"}")
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Lowongan ID: ${lamaran!!.lowonganId}")
                                Text("Tanggal Lamaran: ${formatDate(lamaran!!.tanggalLamaran)}")
                                Text("Status: ${lamaran!!.status.name}")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!lamaran!!.cvBase64.isNullOrEmpty()) {
                            Button(
                                onClick = {
                                    openBase64Pdf(context, lamaran!!.cvBase64!!, "CV_${lamaran!!.userName}.pdf")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Lihat CV")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Text("Ubah Status Lamaran", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedStatus.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Status") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                LamaranStatus.values().forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status.name) },
                                        onClick = {
                                            selectedStatus = status
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.updateStatusLamaran(lamaran!!.id, selectedStatus) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            else Text("Simpan Perubahan Status")
                        }

                        if (updateSuccess) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Status berhasil diperbarui!", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

private fun openBase64Pdf(context: Context, base64: String, fileName: String) {
    try {
        if (base64.isEmpty()) {
            Toast.makeText(context, "CV kosong", Toast.LENGTH_SHORT).show()
            return
        }
        val pdfBytes = Base64.decode(base64, Base64.DEFAULT)
        if (pdfBytes.isEmpty()) {
            Toast.makeText(context, "Data CV tidak valid", Toast.LENGTH_SHORT).show()
            return
        }
        val tempFile = File(context.cacheDir, fileName)
        tempFile.writeBytes(pdfBytes)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Tidak ada aplikasi pembaca PDF", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membuka CV: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun formatDate(timestamp: Long): String {
    val df = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    return df.format(java.util.Date(timestamp))
}
