package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBlueLight
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityCanvas
import com.example.ui.theme.HighDensityNavy
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.MontecarloOrange

data class DtcItem(val code: String, val system: String, val description: String)

@Composable
fun DtcHelperDialog(
    onSelectDtc: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val commonDtcs = listOf(
        DtcItem("P0300", "Pengapian", "Random / Multiple Cylinder Misfire (Mesin Pincang)"),
        DtcItem("P0301", "Pengapian", "Cylinder 1 Misfire Detected"),
        DtcItem("P0303", "Pengapian", "Cylinder 3 Misfire Detected"),
        DtcItem("P0171", "Bahan Bakar", "System Too Lean - Bank 1 (Campuran Bensin Terlalu Miskin/Bocor Vakum)"),
        DtcItem("P0087", "Fuel Rail", "Fuel Rail/System Pressure - Too Low (Tekanan Solar/Bensin Drop)"),
        DtcItem("P0420", "Emisi", "Catalyst System Efficiency Below Threshold (Katalisator Knalpot)"),
        DtcItem("P0700", "Transmisi", "Transmission Control System Malfunction (Modul TCM / Solenoid Matic)"),
        DtcItem("P0705", "Transmisi", "Transmission Range Sensor Circuit Malfunction (Sensor Posisi Tuas P/R/N/D)"),
        DtcItem("P0100", "Sensor Udara", "Mass or Volume Air Flow (MAF) Sensor Circuit Malfunction"),
        DtcItem("P0115", "Suhu Mesin", "Engine Coolant Temperature (ECT) Sensor Circuit Malfunction"),
        DtcItem("P0335", "Sensor Mesin", "Crankshaft Position Sensor 'A' Circuit (Sensor Kruk As / CKP)"),
        DtcItem("P0340", "Sensor Mesin", "Camshaft Position Sensor Circuit Malfunction (Sensor Noken As / CMP)"),
        DtcItem("C1201", "Rem & ABS", "Engine Control System Malfunction / ABS Sensor Speed Input Circuit"),
        DtcItem("B1325", "Body & Airbag", "Device Power Circuit Low Voltage (Tegangan Suplai Modul Drop)")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dtc_helper_dialog")
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(HighDensityBlueLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = HighDensityBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pilih Kode DTC Standar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityNavy
                            )
                            Text(
                                text = "Diagnostic Trouble Codes OBD-2",
                                style = MaterialTheme.typography.bodySmall,
                                color = HighDensityTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dtc_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = HighDensityNavy)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(commonDtcs) { dtc ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(HighDensityCanvas)
                                .border(1.dp, HighDensityBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    onSelectDtc(dtc.code)
                                    onDismiss()
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(HighDensityNavy, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = dtc.code,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dtc.system,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HighDensityBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = dtc.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HighDensityTextPrimary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
