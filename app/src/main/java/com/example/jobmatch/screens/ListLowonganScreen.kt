package com.example.jobmatch.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.jobmatch.model.Lowongan
import com.example.jobmatch.navigation.Screen
import com.example.jobmatch.ui.theme.ScrollableScreen
import com.example.jobmatch.viewmodel.LowonganViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListLowonganScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    viewModel: LowonganViewModel = viewModel()
) {
    val allLowongan by viewModel.lowonganList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLokasi by remember { mutableStateOf("") }
    var selectedPendidikan by remember { mutableStateOf("") }

    val filteredLowongan = remember(allLowongan, searchQuery, selectedLokasi, selectedPendidikan) {
        allLowongan.filter { lowongan ->
            (searchQuery.isEmpty() || lowongan.judul.contains(searchQuery, ignoreCase = true)) &&
                    (selectedLokasi.isEmpty() || lowongan.lokasi.equals(selectedLokasi, ignoreCase = true)) &&
                    (selectedPendidikan.isEmpty() || lowongan.pendidikanMin.equals(selectedPendidikan, ignoreCase = true))
        }
    }

    val uniqueLokasi = remember(allLowongan) { allLowongan.map { it.lokasi }.distinct() }
    val uniquePendidikan = remember(allLowongan) { allLowongan.map { it.pendidikanMin }.distinct() }

    LaunchedEffect(Unit) {
        viewModel.loadAllLowongan(onlyVerified = true)
    }

    ScrollableScreen(
        title = "Daftar Lowongan",
        onBackClick = { navController.popBackStack() },
        isRefreshing = isLoading,
        onRefresh = { viewModel.loadAllLowongan(onlyVerified = true) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari lowongan...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterDropdown(
                    label = "Lokasi",
                    options = uniqueLokasi,
                    selected = selectedLokasi,
                    onSelect = { selectedLokasi = it },
                    modifier = Modifier.weight(1f)  // weight diberikan di sini
                )
                FilterDropdown(
                    label = "Pendidikan Min",
                    options = uniquePendidikan,
                    selected = selectedPendidikan,
                    onSelect = { selectedPendidikan = it },
                    modifier = Modifier.weight(1f)
                )
                if (selectedLokasi.isNotEmpty() || selectedPendidikan.isNotEmpty()) {
                    TextButton(onClick = {
                        selectedLokasi = ""
                        selectedPendidikan = ""
                    }) {
                        Text("Reset")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }
                filteredLowongan.isEmpty() -> {
                    Text("Tidak ada lowongan yang sesuai.")
                }
                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        filteredLowongan.forEach { lowongan ->
                            LowonganCard(lowongan = lowongan) {
                                navController.navigate(Screen.DetailLowongan.passId(lowongan.id))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier  // modifier diterapkan ke menu box
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LowonganCard(lowongan: Lowongan, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lowongan.judul, 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = lowongan.companyName, 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    VerificationBadge(isVerified = lowongan.companyVerified)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconText(icon = Icons.Default.LocationOn, text = lowongan.lokasi)
                IconText(icon = Icons.Default.Payments, text = lowongan.gaji)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(
                    text = "Min. ${lowongan.pendidikanMin}", 
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun VerificationBadge(isVerified: Boolean) {
    Surface(
        color = if (isVerified) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = if (isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (isVerified) "Terverifikasi" else "Belum Terverifikasi",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun IconText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}