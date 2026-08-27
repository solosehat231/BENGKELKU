package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TicketUrgency
import com.example.ui.components.CameraCaptureDialog
import com.example.ui.components.DtcHelperDialog
import com.example.ui.components.VoiceInputOverlay
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBlueLight
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityCanvas
import com.example.ui.theme.HighDensityNavy
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.MontecarloOrange
import com.example.ui.theme.StatusActiveAmber
import com.example.ui.theme.StatusUrgentRed
import com.example.ui.viewmodel.BengkelViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateTicketScreen(
    viewModel: BengkelViewModel,
    onNavigateBack: () -> Unit,
    onTicketCreated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Form fields
    val carBrands = listOf("Toyota", "Honda", "Mitsubishi", "Daihatsu", "Suzuki", "Hyundai", "Wuling", "Nissan")
    val carModelsMap = mapOf(
        "Toyota" to listOf("Avanza 1.3/1.5 Dual VVT-i", "Innova Reborn 2.4 Diesel", "Innova Zenix Hybrid", "Fortuner 2.8 VRZ", "Calya 1.2 G", "Yaris Cross Hybrid", "Rush 1.5 S"),
        "Honda" to listOf("Brio RS 1.2 CVT", "HR-V 1.5 Turbo RS", "CR-V 2.0 e:HEV Hybrid", "City Hatchback RS", "BR-V N7X Edition", "Civic Turbo"),
        "Mitsubishi" to listOf("Pajero Sport Dakar 4x2", "Xpander 1.5 Ultimate", "Xpander Cross CVT", "Xforce 1.5 Exceed", "Triton 2.5 HDX"),
        "Daihatsu" to listOf("Sigra 1.2 R Deluxe", "Xenia 1.3/1.5 Dual VVT-i", "Terios 1.5 R Custom", "Rocky 1.0 Turbo", "GranMax 1.5 Pick Up"),
        "Suzuki" to listOf("Ertiga Dreza / Hybrid", "XL7 Alpha Hybrid", "Jimny 5-Door 4x4", "Baleno Hatchback", "Carry Pick Up 1.5"),
        "Hyundai" to listOf("Creta 1.5 Prime", "Stargazer X Prime", "Ioniq 5 Signature EV", "Santa Fe 2.2 CRDi Diesel", "Palisade 2.2 AWD"),
        "Wuling" to listOf("Air EV Long Range", "Alvez 1.5 EX", "Almaz RS Pro Hybrid", "Confero S 1.5 C"),
        "Nissan" to listOf("Grand Livina 1.5 XV", "Serena C27 Highway Star", "Kicks e-Power", "Terra 2.5 4x4 Diesel")
    )

    var selectedBrand by remember { mutableStateOf("Toyota") }
    var selectedModel by remember { mutableStateOf("Avanza 1.3/1.5 Dual VVT-i") }
    var brandExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }

    var licensePlate by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2021") }
    var dtcCode by remember { mutableStateOf("") }
    var complaintText by remember { mutableStateOf("") }
    var selectedUrgency by remember { mutableStateOf(TicketUrgency.NORMAL) }
    var selectedCategory by remember { mutableStateOf("Mesin & Pengapian") }
    var attachedPhotoUri by remember { mutableStateOf<String?>(null) }

    // Dialog state
    var showDtcHelper by remember { mutableStateOf(false) }
    var showVoiceOverlay by remember { mutableStateOf(false) }
    var showCameraDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "Mesin & Pengapian",
        "Transmisi / Matic CVT",
        "Kelistrikan & ECU",
        "Kaki-Kaki & Rem ABS",
        "AC & Pendingin",
        "Bahan Bakar & Sensor"
    )

    if (showDtcHelper) {
        DtcHelperDialog(
            onSelectDtc = { code -> dtcCode = code },
            onDismiss = { showDtcHelper = false }
        )
    }

    if (showVoiceOverlay) {
        VoiceInputOverlay(
            onVoiceTextRecorded = { text ->
                complaintText = if (complaintText.isBlank()) text else "$complaintText $text"
            },
            onDismiss = { showVoiceOverlay = false }
        )
    }

    if (showCameraDialog) {
        CameraCaptureDialog(
            onPhotoCaptured = { uri -> attachedPhotoUri = uri },
            onDismiss = { showCameraDialog = false }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Buat Tiket Kendala",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityNavy
                        )
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
                .background(HighDensityCanvas)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag("create_ticket_screen")
        ) {
            // Quick Tip Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = HighDensityBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Form input instan: Cukup pilih tipe mobil, kode DTC, atau gunakan suara (Voice-to-Text) saat di kolong mobil.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HighDensityTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Data Kendaraan
            Text(
                text = "1. DATA KENDARAAN",
                style = MaterialTheme.typography.labelSmall,
                color = HighDensityNavy,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown Merek Kendaraan
            ExposedDropdownMenuBox(
                expanded = brandExpanded,
                onExpandedChange = { brandExpanded = !brandExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedBrand,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Merek Kendaraan", color = HighDensityTextSecondary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("brand_dropdown"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityBlue,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = HighDensityTextPrimary,
                        unfocusedTextColor = HighDensityTextPrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = brandExpanded,
                    onDismissRequest = { brandExpanded = false }
                ) {
                    carBrands.forEach { brand ->
                        DropdownMenuItem(
                            text = { Text(brand, color = HighDensityNavy) },
                            onClick = {
                                selectedBrand = brand
                                selectedModel = carModelsMap[brand]?.firstOrNull() ?: ""
                                brandExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dropdown Tipe Kendaraan
            ExposedDropdownMenuBox(
                expanded = modelExpanded,
                onExpandedChange = { modelExpanded = !modelExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipe & Varian Model", color = HighDensityTextSecondary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("model_dropdown"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityBlue,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = HighDensityTextPrimary,
                        unfocusedTextColor = HighDensityTextPrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = modelExpanded,
                    onDismissRequest = { modelExpanded = false }
                ) {
                    carModelsMap[selectedBrand]?.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model, color = HighDensityNavy) },
                            onClick = {
                                selectedModel = model
                                modelExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Plate & Year Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it },
                    label = { Text("Nomor Plat (Opsional)", color = HighDensityTextSecondary) },
                    placeholder = { Text("AD 1234 XY") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("plate_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityBlue,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = HighDensityTextPrimary,
                        unfocusedTextColor = HighDensityTextPrimary
                    )
                )

                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Tahun", color = HighDensityTextSecondary) },
                    modifier = Modifier
                        .width(100.dp)
                        .testTag("year_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityBlue,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = HighDensityTextPrimary,
                        unfocusedTextColor = HighDensityTextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Diagnosa & Kode DTC
            Text(
                text = "2. DIAGNOSTIK & KODE DTC (OPSIONAL)",
                style = MaterialTheme.typography.labelSmall,
                color = HighDensityNavy,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // DTC Input with Quick Scanner Helper
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = dtcCode,
                    onValueChange = { dtcCode = it },
                    label = { Text("Kode DTC OBD-2", color = HighDensityTextSecondary) },
                    placeholder = { Text("e.g. P0300, P0171, P0420") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dtc_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityBlue,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = HighDensityTextPrimary,
                        unfocusedTextColor = HighDensityTextPrimary
                    )
                )

                Button(
                    onClick = { showDtcHelper = true },
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("open_dtc_helper_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pilih DTC", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Keluhan & Voice-to-Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3. KELUHAN & GEJALA KENDARAAN",
                    style = MaterialTheme.typography.labelSmall,
                    color = HighDensityNavy,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Bold
                )

                // Voice-to-Text Trigger Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MontecarloOrange.copy(alpha = 0.12f))
                        .clickable { showVoiceOverlay = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MontecarloOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bicara (Suara)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MontecarloOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large text area for complaint
            OutlinedTextField(
                value = complaintText,
                onValueChange = { complaintText = it },
                placeholder = {
                    Text(
                        "Ceritakan kronologi kerusakan, kondisi mesin saat gejala muncul (misal: rpm berapa, getaran, suhu, bunyi ketukan, atau riwayat servis sebelumnya)...",
                        fontSize = 13.sp,
                        color = HighDensityTextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("complaint_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HighDensityBlue,
                    unfocusedBorderColor = HighDensityBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = HighDensityTextPrimary,
                    unfocusedTextColor = HighDensityTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Kamera Terintegrasi
            Text(
                text = "4. FOTO KOMPONEN RUSAK",
                style = MaterialTheme.typography.labelSmall,
                color = HighDensityNavy,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (attachedPhotoUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0FDF4), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Foto Komponen Terlampir (Siap kirim ke 18 cabang)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { attachedPhotoUri = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus",
                                tint = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showCameraDialog = true },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = HighDensityBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Buka Kamera (Foto Komponen / Indikator)",
                            style = MaterialTheme.typography.titleSmall,
                            color = HighDensityNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section: Tingkat Urgensi
            Text(
                text = "5. TINGKAT URGENSI",
                style = MaterialTheme.typography.labelSmall,
                color = HighDensityNavy,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TicketUrgency.entries.forEach { urgency ->
                    val isSelected = selectedUrgency == urgency
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) {
                                    when (urgency) {
                                        TicketUrgency.EMERGENCY_MOGOK -> StatusUrgentRed
                                        TicketUrgency.URGENT -> StatusActiveAmber
                                        TicketUrgency.NORMAL -> HighDensityBlue
                                    }
                                } else Color.White
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else HighDensityBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedUrgency = urgency }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = urgency.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White else HighDensityNavy,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    val finalComplaint = complaintText.ifBlank {
                        "Kendala komponen pada unit $selectedBrand $selectedModel ${if (dtcCode.isNotBlank()) "dengan kode DTC $dtcCode" else ""}. Membutuhkan rekomendasi diagnosa dan part."
                    }
                    viewModel.createNewTicket(
                        brand = selectedBrand,
                        model = selectedModel,
                        plate = licensePlate.ifBlank { "BENGKEL-TEST" },
                        year = year,
                        dtc = dtcCode,
                        complaint = finalComplaint,
                        urgency = selectedUrgency,
                        category = selectedCategory,
                        photoUri = attachedPhotoUri,
                        onSuccess = { ticketId ->
                            onTicketCreated(ticketId)
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_ticket_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MontecarloOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buka Tiket & Sambungkan ke 18 Cabang",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
