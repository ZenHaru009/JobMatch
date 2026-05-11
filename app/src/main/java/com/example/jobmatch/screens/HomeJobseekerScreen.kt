package com.example.jobmatch.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import com.example.jobmatch.model.Lowongan
import com.example.jobmatch.navigation.Screen
import com.example.jobmatch.ui.theme.ScrollableScreen
import com.example.jobmatch.viewmodel.AuthViewModel
import com.example.jobmatch.viewmodel.RecommendationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeJobseekerScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    authViewModel: AuthViewModel = viewModel(),
    recommendationViewModel: RecommendationViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val recommendations by recommendationViewModel.recommendations.collectAsState()
    val isLoading by recommendationViewModel.isLoading.collectAsState()
    val error by recommendationViewModel.error.collectAsState()

    // Load rekomendasi ketika user tersedia dan profil sudah lengkap
    LaunchedEffect(currentUser) {
        if (currentUser != null && currentUser?.profileCompleted == true) {
            recommendationViewModel.loadRecommendations()
        }
    }

    ScrollableScreen(
        title = "JobMatch - Pelamar",
        showBackButton = false // Matikan tombol kembali di Beranda
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ucapan Selamat Datang
            currentUser?.let { user ->
                Column {
                    Text(
                        text = "Halo, ${user.name.ifEmpty { "Pencari Kerja" }}!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    if (user.profileCompleted) {
                        Text(
                            text = "Profil Anda sudah lengkap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "Lengkapi profil Anda untuk mendapatkan rekomendasi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bagian Rekomendasi
            Text(
                "Rekomendasi Pekerjaan", 
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Berdasarkan profil dan keahlian Anda",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }
                recommendations.isEmpty() -> {
                    Text("Belum ada rekomendasi. Lengkapi profil Anda terlebih dahulu.")
                }
                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        recommendations.take(5).forEach { (lowongan, score) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate(Screen.DetailLowongan.passId(lowongan.id))
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            lowongan.judul, 
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            lowongan.companyName, 
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        VerificationBadge(isVerified = lowongan.companyVerified)

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            lowongan.lokasi, 
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = "${(score * 100).toInt()}% Match",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol-tombol lain
            FilledTonalButton(
                onClick = { navController.navigate(Screen.ListLowongan.route) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Lihat Semua Lowongan")
            }

            FilledTonalButton(
                onClick = { navController.navigate(Screen.JobSeekerProfile.route) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (currentUser?.profileCompleted == true) "Edit Profil" else "Lengkapi Profil")
            }

            FilledTonalButton(
                onClick = { navController.navigate(Screen.Tracking.route) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Status Lamaran Saya")
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.HomeJobseeker.route) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Logout")
            }
        }
    }
}
