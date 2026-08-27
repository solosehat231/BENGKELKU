package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatMessageEntity
import com.example.data.model.TicketStatus
import com.example.data.model.TicketUrgency
import com.example.ui.components.CameraCaptureDialog
import com.example.ui.components.SmartSopAlertCard
import com.example.ui.components.VoiceInputOverlay
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBlueLight
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityCanvas
import com.example.ui.theme.HighDensityNavy
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.MontecarloOrange
import com.example.ui.theme.StatusSolvedGreen
import com.example.ui.theme.StatusUrgentRed
import com.example.ui.viewmodel.BengkelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    viewModel: BengkelViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ticket by viewModel.activeTicket.collectAsStateWithLifecycle()
    val messages by viewModel.ticketMessages.collectAsStateWithLifecycle()
    val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()

    var inputMessage by remember { mutableStateOf("") }
    var showResolveDialog by remember { mutableStateOf(false) }
    var showDetailUnitDialog by remember { mutableStateOf(false) }
    var resolveSummaryInput by remember { mutableStateOf("") }
    var showVoiceOverlay by remember { mutableStateOf(false) }
    var showCameraDialog by remember { mutableStateOf(false) }
    // Default collapsed agar tidak menutupi area chat di layar HP
    var isCaseDetailExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isAiGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showVoiceOverlay) {
        VoiceInputOverlay(
            onVoiceTextRecorded = { text ->
                inputMessage = if (inputMessage.isBlank()) text else "$inputMessage $text"
            },
            onDismiss = { showVoiceOverlay = false }
        )
    }

    if (showCameraDialog) {
        CameraCaptureDialog(
            onPhotoCaptured = {
                ticket?.id?.let { tId ->
                    viewModel.sendMechanicMessage(tId, "📸 [Lampiran Foto Komponen Terkirim ke Diskusi 18 Cabang]")
                }
            },
            onDismiss = { showCameraDialog = false }
        )
    }

    // Modal Dialog Rincian Lengkap Unit (Muncul saat ditekan tanpa memakan ruang chat di layar ponsel)
    if (showDetailUnitDialog && ticket != null) {
        val t = ticket!!
        Dialog(onDismissRequest = { showDetailUnitDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = HighDensityBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rincian Unit & Kendala",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityNavy
                            )
                        }
                        IconButton(onClick = { showDetailUnitDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = HighDensityNavy)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CaseDetailTableRow("Kendaraan", "${t.vehicleBrand} ${t.vehicleModel}", "Tahun", t.year.toString())
                    HorizontalDivider(color = HighDensityBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 6.dp))
                    CaseDetailTableRow("No. Polisi", t.licensePlate, "Kode DTC", t.dtcCode.ifBlank { "Tidak Ada" })
                    HorizontalDivider(color = HighDensityBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 6.dp))
                    CaseDetailTableRow("Cabang Asal", t.branchName, "Mekanik", t.mechanicName)
                    HorizontalDivider(color = HighDensityBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 6.dp))

                    Text(
                        text = "Keluhan Utama Kerusakan:",
                        style = MaterialTheme.typography.labelSmall,
                        color = HighDensityTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = t.complaint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = HighDensityNavy,
                        lineHeight = 18.sp
                    )

                    if (t.status == TicketStatus.RESOLVED && t.solutionSummary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "✅ Solusi Final Terverifikasi:\n${t.solutionSummary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF166534),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showDetailUnitDialog = false },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityNavy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Kembali ke Diskusi", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Dialog Tandai Selesai
    if (showResolveDialog && ticket != null) {
        Dialog(onDismissRequest = { showResolveDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("resolve_ticket_dialog"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusSolvedGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tandai Tiket Selesai",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityNavy
                            )
                        }
                        IconButton(onClick = { showResolveDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = HighDensityNavy)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Rangkum solusi perbaikan agar mekanik lain di 18 cabang dapat mempelajarinya di fitur 'Cari Solusi Cepat':",
                        style = MaterialTheme.typography.bodySmall,
                        color = HighDensityTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = resolveSummaryInput,
                        onValueChange = { resolveSummaryInput = it },
                        placeholder = {
                            Text("Misal: Mengganti Koil silinder 3 + busi baru Iridium denso. Masalah brebet & misfire teratasi tuntas.", color = HighDensityTextSecondary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("resolve_summary_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityBlue,
                            unfocusedBorderColor = HighDensityBorder,
                            focusedTextColor = HighDensityTextPrimary,
                            unfocusedTextColor = HighDensityTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val finalSol = resolveSummaryInput.ifBlank {
                                "Masalah telah berhasil diperbaiki dan diverifikasi oleh tim mekanik."
                            }
                            viewModel.markTicketResolved(ticket!!.id, finalSol)
                            showResolveDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_resolve_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusSolvedGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simpan Solusi & Tutup Tiket",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = ticket?.ticketNumber ?: "Ruang Diskusi",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityNavy
                            )
                            Text(
                                text = "${ticket?.vehicleBrand ?: ""} ${ticket?.vehicleModel ?: ""} (${ticket?.licensePlate ?: ""})",
                                style = MaterialTheme.typography.bodySmall,
                                color = HighDensityTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = HighDensityNavy
                            )
                        }
                    },
                    actions = {
                        // Tombol Info Unit
                        IconButton(onClick = { showDetailUnitDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info Detail Mobil",
                                tint = HighDensityBlue
                            )
                        }

                        // Tombol Selesai
                        if (ticket != null && ticket?.status != TicketStatus.RESOLVED) {
                            Button(
                                onClick = { showResolveDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusSolvedGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .testTag("mark_resolved_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Selesai",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                HorizontalDivider(color = HighDensityBorder, thickness = 1.dp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .background(HighDensityCanvas)
                .testTag("ticket_detail_screen")
        ) {
            // 1. COMPACT HEADER BAR KENDARAAN (Sangat Ramping & Tidak Menghalangi Ruang Chat di HP)
            if (ticket != null) {
                val t = ticket!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Slim Header Strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HighDensityCanvas)
                                .clickable { isCaseDetailExpanded = !isCaseDetailExpanded }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = HighDensityBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${t.vehicleBrand} ${t.vehicleModel} • ${t.branchName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityNavy,
                                    maxLines = 1
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (t.dtcCode.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .background(HighDensityBlueLight, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = t.dtcCode,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HighDensityBlue,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (t.urgency == TicketUrgency.EMERGENCY_MOGOK) StatusUrgentRed else MontecarloOrange,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = t.urgency.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = if (isCaseDetailExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = HighDensityTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Collapsible Table Grid Details (Hanya muncul jika mekanik ingin melihat rincian tambahan)
                        AnimatedVisibility(
                            visible = isCaseDetailExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                CaseDetailTableRow(
                                    label1 = "No. Polisi",
                                    value1 = t.licensePlate,
                                    label2 = "Tahun",
                                    value2 = t.year.toString()
                                )

                                HorizontalDivider(color = HighDensityBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                                CaseDetailTableRow(
                                    label1 = "Cabang Pelapor",
                                    value1 = t.branchName,
                                    label2 = "Mekanik",
                                    value2 = t.mechanicName
                                )

                                HorizontalDivider(color = HighDensityBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                                Text(
                                    text = "Keluhan Kerusakan:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = HighDensityTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = t.complaint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HighDensityNavy,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )

                                if (t.status == TicketStatus.RESOLVED && t.solutionSummary.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF0FDF4), RoundedCornerShape(6.dp))
                                            .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(6.dp))
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = "✅ Solusi: ${t.solutionSummary}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF166534),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. TIMELINE TABEL DISKUSI (Bebas, Luas, & Responsif di Layar HP)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubbleItem(
                            message = msg,
                            onViewSop = onNavigateToSop
                        )
                    }

                    if (isAiGenerating) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, HighDensityBlue, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = HighDensityBlue,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI Gemini sedang menganalisa data...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HighDensityNavy,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. BARIS PROMPT SARAN CEPAT AI (Kompak)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, HighDensityBorder)
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val quickAiPrompts = listOf(
                        "⚡ Analisa DTC" to "Mohon analisa kode DTC dan gejala kendala pada mobil ini secara komprehensif.",
                        "🔍 Cek Tahanan Koil" to "Berapa nilai standar resistansi koil pengapian dan celah busi untuk mesin unit ini?",
                        "📊 Uji Sensor O2" to "Bagaimana langkah menguji sinyal tegangan O2 Sensor dan Heater menggunakan Multitester?",
                        "📋 Langkah Troubleshooting" to "Tolong urutkan langkah perbaikan bertahap dari yang paling mudah dan hemat biaya."
                    )

                    items(quickAiPrompts) { (label, prompt) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HighDensityBlueLight)
                                .border(1.dp, HighDensityBlue.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                .clickable {
                                    inputMessage = prompt
                                }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = HighDensityNavy,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(color = HighDensityBorder.copy(alpha = 0.4f), thickness = 0.5.dp)

                // 4. CHAT INPUT BAR DENGAN TOMBOL TANYA AI TERINTEGRASI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tombol Kamera Foto
                    IconButton(
                        onClick = { showCameraDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(HighDensityCanvas, CircleShape)
                            .border(1.dp, HighDensityBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Kamera",
                            tint = HighDensityBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Tombol Voice Suara
                    IconButton(
                        onClick = { showVoiceOverlay = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(HighDensityCanvas, CircleShape)
                            .border(1.dp, HighDensityBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice",
                            tint = MontecarloOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Input Text
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = { Text("Ketik pesan...", color = HighDensityTextSecondary, fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = HighDensityTextPrimary,
                            unfocusedTextColor = HighDensityTextPrimary,
                            focusedContainerColor = HighDensityCanvas,
                            unfocusedContainerColor = HighDensityCanvas,
                            focusedBorderColor = HighDensityBlue,
                            unfocusedBorderColor = HighDensityBorder
                        ),
                        singleLine = false,
                        maxLines = 3
                    )

                    // Tombol "Tanya AI" (Terintegrasi)
                    Button(
                        onClick = {
                            ticket?.id?.let { tId ->
                                val question = inputMessage.ifBlank { "Mohon berikan analisa dan rekomendasi perbaikan untuk kasus ini." }
                                inputMessage = ""
                                viewModel.askAiAssistant(tId, question)
                            }
                        },
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("integrated_ask_ai_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighDensityBlue,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = "Tanya AI",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Tanya AI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Tombol Kirim Diskusi Mekanik
                    IconButton(
                        onClick = {
                            if (inputMessage.isNotBlank() && ticket != null) {
                                val textToSend = inputMessage
                                inputMessage = ""
                                viewModel.sendMechanicMessage(ticket!!.id, textToSend)
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(MontecarloOrange, CircleShape)
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CaseDetailTableRow(
    label1: String,
    value1: String,
    label2: String,
    value2: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label1,
                style = MaterialTheme.typography.labelSmall,
                color = HighDensityTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value1.ifBlank { "-" },
                style = MaterialTheme.typography.bodySmall,
                color = HighDensityNavy,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label2,
                style = MaterialTheme.typography.labelSmall,
                color = HighDensityTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value2.ifBlank { "-" },
                style = MaterialTheme.typography.bodySmall,
                color = HighDensityNavy,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessageEntity,
    onViewSop: () -> Unit
) {
    val isAi = message.senderType == "AI_MASTER"
    val isMe = message.senderType == "MECHANIC"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = when {
            isMe -> Alignment.End
            else -> Alignment.Start
        }
    ) {
        // Sender Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            if (isAi) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(HighDensityBlueLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = HighDensityBlue,
                        modifier = Modifier.size(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Master Mekanik AI (Gemini Diagnostik)",
                    style = MaterialTheme.typography.labelSmall,
                    color = HighDensityBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(if (isMe) HighDensityBlueLight else HighDensityCanvas, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isMe) HighDensityBlue else HighDensityTextSecondary,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isMe) "Anda (${message.branchName.ifBlank { "Cabang" }})" else "${message.senderName} (${message.branchName})",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMe) HighDensityNavy else HighDensityTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Chat Bubble Box
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isAi) 1f else 0.88f)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMe) 12.dp else 3.dp,
                        bottomEnd = if (isMe) 3.dp else 12.dp
                    )
                )
                .background(
                    when {
                        isAi -> Color.White
                        isMe -> HighDensityBlue
                        else -> Color.White
                    }
                )
                .border(
                    width = 1.dp,
                    color = when {
                        isAi -> HighDensityBlue.copy(alpha = 0.5f)
                        isMe -> Color.Transparent
                        else -> HighDensityBorder
                    },
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMe) 12.dp else 3.dp,
                        bottomEnd = if (isMe) 3.dp else 12.dp
                    )
                )
                .padding(10.dp)
        ) {
            Column {
                if (isAi) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = HighDensityBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Rekomendasi Diagnosa Teknis:",
                            style = MaterialTheme.typography.labelSmall,
                            color = HighDensityBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMe) Color.White else HighDensityNavy,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                // SMART SOP WARNING IF TRIGGERED
                if (!message.sopWarning.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    SmartSopAlertCard(
                        alertText = message.sopWarning,
                        onViewSop = onViewSop
                    )
                }
            }
        }
    }
}
