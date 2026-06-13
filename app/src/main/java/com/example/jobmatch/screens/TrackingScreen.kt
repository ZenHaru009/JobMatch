package com.example.jobmatch.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import com.example.jobmatch.model.Lamaran
import com.example.jobmatch.ui.theme.ScrollableScreen
import com.example.jobmatch.viewmodel.TrackingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: TrackingViewModel = viewModel()
) {
    val lamaranList by viewModel.lamaranList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyLamaran()
    }

    ScrollableScreen(
        title = "Status Lamaran Saya",
        onBackClick = { navController.popBackStack() },
        isRefreshing = isLoading,
        onRefresh = { viewModel.loadMyLamaran() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> {
                    Text("Error: $error", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                }
                lamaranList.isEmpty() -> {
                    Text("Belum ada lamaran", modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        lamaranList.forEach { lamaran ->
                            LamaranTrackingCard(lamaran)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LamaranTrackingCard(lamaran: Lamaran) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Lowongan ID: ${lamaran.lowonganId}", style = MaterialTheme.typography.titleMedium)
            Text("Tanggal: ${android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", lamaran.tanggalLamaran)}")
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Status: ")
                when (lamaran.status) {
                    com.example.jobmatch.model.LamaranStatus.DIPROSES -> {
                        Badge { Text("DIPROSES") }
                    }
                    com.example.jobmatch.model.LamaranStatus.INTERVIEW -> {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("INTERVIEW") }
                    }
                    com.example.jobmatch.model.LamaranStatus.DITERIMA -> {
                        Badge(containerColor = MaterialTheme.colorScheme.tertiary) { Text("DITERIMA") }
                    }
                    com.example.jobmatch.model.LamaranStatus.DITOLAK -> {
                        Badge(containerColor = MaterialTheme.colorScheme.error) { Text("DITOLAK") }
                    }
                }
            }
        }
    }
}