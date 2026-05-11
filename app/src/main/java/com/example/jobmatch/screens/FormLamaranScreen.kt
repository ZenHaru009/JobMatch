package com.example.jobmatch.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import com.example.jobmatch.viewmodel.FormLamaranViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormLamaranScreen(
    navController: NavController,
    lowonganId: String,
    windowSizeClass: WindowSizeClass,
    viewModel: FormLamaranViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()

    var userName by remember { mutableStateOf("") }
    var noHp by remember { mutableStateOf("") }          // <-- TAMBAHAN
    var selectedCvUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val cvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedCvUri = uri
    }

    LaunchedEffect(success) {
        if (success) {
            kotlinx.coroutines.delay(1500)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Form Lamaran") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            OutlinedTextField(         // <-- TAMBAHAN INPUT NO HP
                value = noHp,
                onValueChange = { noHp = it },
                label = { Text("Nomor HP (untuk dihubungi perusahaan)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            Button(
                onClick = { cvPickerLauncher.launch("application/pdf") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (selectedCvUri == null) "Pilih CV (PDF)" else "CV Terpilih")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        var cvBase64: String? = null
                        if (selectedCvUri != null) {
                            cvBase64 = viewModel.uriToBase64(selectedCvUri!!, context)
                        }
                        viewModel.submitLamaran(lowonganId, userName, cvBase64, noHp)   // <-- KIRIM NO HP
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && userName.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Kirim Lamaran")
            }

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
            if (success) {
                Text("Lamaran berhasil dikirim!", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}