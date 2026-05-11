package com.example.jobmatch.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import com.example.jobmatch.navigation.Screen
import com.example.jobmatch.ui.theme.ScrollableScreen
import com.example.jobmatch.viewmodel.AdminViewModel
import com.example.jobmatch.viewmodel.AuthViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    adminViewModel: AdminViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val companies by adminViewModel.companies.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val error by adminViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        adminViewModel.loadPendingCompanies()
    }

    ScrollableScreen(
        title = "Verifikasi Perusahaan",
        showBackButton = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Daftar Tunggu", 
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                "Tinjau dokumen dan setujui pendaftaran perusahaan baru.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Error: $error", 
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                companies.isEmpty() -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            "Tidak ada perusahaan yang menunggu verifikasi.",
                            modifier = Modifier.padding(24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        companies.forEach { company ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        company.nama, 
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    AdminInfoRow(icon = Icons.Default.Email, text = company.email)
                                    company.lokasi?.let { AdminInfoRow(icon = Icons.Default.LocationOn, text = it) }
                                    
                                    company.deskripsi?.let {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            it, 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    if (!company.dokumenBase64.isNullOrEmpty()) {
                                        FilledTonalButton(
                                            onClick = {
                                                openBase64Pdf(context, company.dokumenBase64!!, "dokumen_verifikasi_${company.uid}.pdf")
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Icon(Icons.Default.Description, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Lihat Dokumen Verifikasi")
                                        }
                                    } else {
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                            shape = MaterialTheme.shapes.small,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                "Dokumen belum diunggah", 
                                                color = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(8.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = { adminViewModel.updateVerificationStatus(company.uid, "verified") },
                                            modifier = Modifier.weight(1f),
                                            shape = MaterialTheme.shapes.medium,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)) // Success Green
                                        ) {
                                            Text("Setujui")
                                        }
                                        Button(
                                            onClick = { adminViewModel.updateVerificationStatus(company.uid, "rejected") },
                                            modifier = Modifier.weight(1f),
                                            shape = MaterialTheme.shapes.medium,
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text("Tolak")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout Admin")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AdminInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun openBase64Pdf(context: Context, base64: String, fileName: String) {
    try {
        if (base64.isEmpty()) {
            Toast.makeText(context, "Dokumen kosong! Silakan upload ulang.", Toast.LENGTH_SHORT).show()
            return
        }
        val pdfBytes = Base64.decode(base64, Base64.DEFAULT)
        if (pdfBytes.isEmpty()) {
            Toast.makeText(context, "Data dokumen tidak valid (decode gagal).", Toast.LENGTH_SHORT).show()
            return
        }
        // Periksa header PDF (%PDF)
        if (pdfBytes.size < 5 || pdfBytes[0] != 0x25.toByte() || pdfBytes[1] != 0x50.toByte() || pdfBytes[2] != 0x44.toByte() || pdfBytes[3] != 0x46.toByte()) {
            Toast.makeText(context, "File bukan PDF yang valid.", Toast.LENGTH_SHORT).show()
            return
        }
        val tempFile = File(context.cacheDir, fileName)
        tempFile.writeBytes(pdfBytes)
        if (!tempFile.exists() || tempFile.length() == 0L) {
            Toast.makeText(context, "Gagal menyimpan file sementara.", Toast.LENGTH_SHORT).show()
            return
        }
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
            Toast.makeText(context, "Tidak ada aplikasi pembaca PDF. Silakan instal PDF viewer (misal Google PDF Viewer) dan coba lagi.", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Gagal membuka dokumen: ${e.message}", Toast.LENGTH_LONG).show()
    }
}