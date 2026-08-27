package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.BengkelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSolutionPostScreen(
    viewModel: BengkelViewModel,
    onNavigateBack: () -> Unit,
    onSuccess: (Long) -> Unit
) {
    val currentMechanic by viewModel.currentMechanic.collectAsState()

    var title by remember { mutableStateOf("") }
    var vehicleBrand by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var dtcCode by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Mesin & Diesel") }
    var symptomDescription by remember { mutableStateOf("") }
    var rootCause by remember { mutableStateOf("") }
    var solutionSteps by remember { mutableStateOf("") }
    var partsReplaced by remember { mutableStateOf("") }
    var estimatedSavingsOrCost by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "Mesin & Diesel",
        "Matic & Transmisi",
        "Kelistrikan & Sensor",
        "Kaki-Kaki & Rem",
        "AC & Pendingin",
        "Gardan & Steering"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Posting Solusi Kasus",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Bagikan ke 18 Cabang & Raih Reward Owner",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Reward Motivation Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Program Solusi Berhadiah Owner",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        )
                        Text(
                            text = "Posting masalah unik beserta solusi cerdas Anda. Solusi yang membantu cabang lain akan langsung diberi bonus uang tunai (Rp 50.000 - Rp 250.000) oleh Owner Bengkel!",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF78350F))
                        )
                    }
                }
            }

            // Author Identity Info
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Mekanik Pembuat:",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = currentMechanic.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = currentMechanic.branchName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Form Fields
            Text(
                text = "1. Data Kendaraan & Kategori",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            // Vehicle Brand & Model Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = vehicleBrand,
                    onValueChange = { vehicleBrand = it },
                    label = { Text("Merek Mobil") },
                    placeholder = { Text("Misal: Toyota") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_vehicle_brand"),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = vehicleModel,
                    onValueChange = { vehicleModel = it },
                    label = { Text("Model & Tipe") },
                    placeholder = { Text("Misal: Innova Reborn Diesel") },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("input_vehicle_model"),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Year & DTC Code Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Tahun") },
                    placeholder = { Text("Misal: 2020") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_vehicle_year"),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = dtcCode,
                    onValueChange = { dtcCode = it },
                    label = { Text("Kode DTC / Gejala") },
                    placeholder = { Text("Misal: P0087 / P0300") },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("input_dtc_code"),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Category Chips Selection
            Text(
                text = "Kategori Kerusakan",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val row1 = categories.take(3)
                val row2 = categories.drop(3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row1.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row2.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Section 2: Case Details
            Text(
                text = "2. Uraian Masalah & Solusi Tepat",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            // Post Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul Postingan Solusi") },
                placeholder = { Text("Misal: Trik Atasi Innova Brebet P0087 Tanpa Ganti Supply Pump") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_solution_title"),
                shape = RoundedCornerShape(8.dp)
            )

            // Symptom Description
            OutlinedTextField(
                value = symptomDescription,
                onValueChange = { symptomDescription = it },
                label = { Text("Gejala / Masalah yang Dijumpai") },
                placeholder = { Text("Jelaskan kondisi mobil saat masuk bengkel, keluhan konsumen, kecepatan, kondisi jalan, atau indikator dashboard...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("input_symptom"),
                shape = RoundedCornerShape(8.dp)
            )

            // Root Cause
            OutlinedTextField(
                value = rootCause,
                onValueChange = { rootCause = it },
                label = { Text("Penyebab Utama / Kerusakan (Root Cause)") },
                placeholder = { Text("Komponen apa yang aus, kotor, korslet, terjepit, atau kendor...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .testTag("input_root_cause"),
                shape = RoundedCornerShape(8.dp)
            )

            // Step-by-Step Solution
            OutlinedTextField(
                value = solutionSteps,
                onValueChange = { solutionSteps = it },
                label = { Text("Langkah-Langkah Solusi Penanganan Sukses") },
                placeholder = { Text("1. Bongkar bagian...\n2. Bersihkan menggunakan...\n3. Kalibrasi via scanner...\n4. Hasil tes jalan...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("input_solution_steps"),
                shape = RoundedCornerShape(8.dp)
            )

            // Parts Replaced / Efficiency
            OutlinedTextField(
                value = partsReplaced,
                onValueChange = { partsReplaced = it },
                label = { Text("Part yang Diganti / Dibersihkan") },
                placeholder = { Text("Misal: O-ring SCV + Pembersihan plunger (Tanpa ganti kompresor/supply pump)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_parts_replaced"),
                shape = RoundedCornerShape(8.dp)
            )

            // Estimated Cost / Savings
            OutlinedTextField(
                value = estimatedSavingsOrCost,
                onValueChange = { estimatedSavingsOrCost = it },
                label = { Text("Efisiensi Biaya / Estimasi Jasa & Part") },
                placeholder = { Text("Misal: Hemat Rp 3.500.000 (Konsumen sangat puas)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_cost_savings"),
                shape = RoundedCornerShape(8.dp)
            )

            // Error display
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Submit Button
            Button(
                onClick = {
                    if (symptomDescription.isBlank() || solutionSteps.isBlank()) {
                        errorMessage = "Mohon isi gejala masalah dan langkah solusinya!"
                        return@Button
                    }
                    errorMessage = null

                    val finalTitle = title.ifBlank {
                        "Solusi Kendala ${vehicleBrand.ifBlank { "Mobil" }} ${vehicleModel.ifBlank { "" }} ${if (dtcCode.isNotBlank()) "($dtcCode)" else ""}".trim()
                    }

                    viewModel.createSolutionPost(
                        title = finalTitle,
                        brand = vehicleBrand.ifBlank { "Umum" },
                        model = vehicleModel.ifBlank { "Semua Tipe" },
                        year = year.ifBlank { "2020" },
                        dtc = dtcCode,
                        category = category,
                        symptom = symptomDescription,
                        rootCause = rootCause.ifBlank { "Faktor pemakaian & kotoran" },
                        steps = solutionSteps,
                        parts = partsReplaced.ifBlank { "Pembersihan & Penyetelan Standar" },
                        costOrSavings = estimatedSavingsOrCost.ifBlank { "Efisiensi Jasa & Part" },
                        onSuccess = { postId ->
                            onSuccess(postId)
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_submit_solution"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Publikasikan Solusi ke 18 Cabang",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
