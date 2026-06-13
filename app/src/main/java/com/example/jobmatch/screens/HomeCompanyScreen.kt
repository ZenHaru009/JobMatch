package com.example.jobmatch.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jobmatch.navigation.Screen
import com.example.jobmatch.ui.theme.ScrollableScreen
import com.example.jobmatch.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCompanyScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    authViewModel: AuthViewModel = viewModel()
) {
    ScrollableScreen(
        title = "JobMatch - Perusahaan",
        showBackButton = false // Matikan tombol kembali di Beranda
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Manajemen Lowongan", 
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.align(Alignment.Start)
            )
            
            Text(
                "Kelola lowongan pekerjaan dan pelamar Anda di sini.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol Buat Lowongan Baru
            Button(
                onClick = { navController.navigate(Screen.AddEditLowongan.passId("new")) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Buat Lowongan Baru", style = MaterialTheme.typography.titleMedium)
            }

            // Tombol Daftar Lowongan Saya (baru)
            FilledTonalButton(
                onClick = { navController.navigate(Screen.CompanyLowonganList.route) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Work, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Daftar Lowongan Saya")
            }

            // Tombol Lihat Semua Lamaran
            FilledTonalButton(
                onClick = { navController.navigate(Screen.CompanyLamaranList.route) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.People, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Lihat Semua Lamaran")
            }

            // Tombol Profil Perusahaan
            FilledTonalButton(
                onClick = { navController.navigate(Screen.CompanyProfile.route) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Business, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Profil Perusahaan")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tombol Logout
            OutlinedButton(
                onClick = { 
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}
