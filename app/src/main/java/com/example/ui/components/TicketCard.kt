package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TicketEntity
import com.example.data.model.TicketStatus
import com.example.data.model.TicketUrgency
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBlueLight
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityBranchAvatar
import com.example.ui.theme.HighDensityNavy
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.MontecarloOrange
import com.example.ui.theme.StatusActiveAmber
import com.example.ui.theme.StatusActiveAmberBg
import com.example.ui.theme.StatusSolvedGreen
import com.example.ui.theme.StatusSolvedGreenBg
import com.example.ui.theme.StatusSolvedGreenText
import com.example.ui.theme.StatusUrgentRed
import com.example.ui.theme.StatusUrgentRedBg

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TicketCard(
    ticket: TicketEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isResolved = ticket.status == TicketStatus.RESOLVED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ticket_card_${ticket.id}")
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (ticket.urgency == TicketUrgency.EMERGENCY_MOGOK && !isResolved)
                    StatusUrgentRed.copy(alpha = 0.5f)
                else
                    HighDensityBorder
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left Branch Avatar Circle (e.g. C4, C2, C18)
            val branchDigits = ticket.branchName.filter { it.isDigit() }
            val branchShort = when {
                ticket.branchId > 0 -> "C${ticket.branchId}"
                branchDigits.isNotEmpty() -> "C$branchDigits"
                else -> "CBG"
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(HighDensityBranchAvatar),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = branchShort,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityTextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Header Row: Vehicle Title and Status/Urgency Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${ticket.vehicleBrand} ${ticket.vehicleModel}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityTextPrimary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (isResolved) {
                        Box(
                            modifier = Modifier
                                .background(StatusSolvedGreenBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SELESAI",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusSolvedGreenText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    } else {
                        val (bgColor, textColor, label) = when (ticket.urgency) {
                            TicketUrgency.EMERGENCY_MOGOK -> Triple(StatusUrgentRedBg, StatusUrgentRed, "DARURAT")
                            TicketUrgency.URGENT -> Triple(StatusActiveAmberBg, StatusActiveAmber, "URGENT")
                            TicketUrgency.NORMAL -> Triple(HighDensityBlueLight, HighDensityNavy, if (ticket.repliesCount > 0) "DISKUSI" else "BARU")
                        }
                        Box(
                            modifier = Modifier
                                .background(bgColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Subtitle metadata: Mechanic & DTC Code / Branch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Mekanik: ${ticket.mechanicName} • ${ticket.branchName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HighDensityTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (ticket.dtcCode.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .background(HighDensityNavy, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = ticket.dtcCode,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Complaint description
                Text(
                    text = ticket.complaint,
                    style = MaterialTheme.typography.bodySmall,
                    color = HighDensityTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                // Solution Banner if Solved
                if (isResolved && ticket.solutionSummary.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StatusSolvedGreenBg, RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "💡 Solusi: ${ticket.solutionSummary}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusSolvedGreenText,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row: Plate, Category, & Reply Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${ticket.licensePlate} • ${ticket.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HighDensityTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    )

                    Surface(
                        color = HighDensityBlueLight.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = HighDensityBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${ticket.repliesCount} Tanggapan",
                                style = MaterialTheme.typography.labelSmall,
                                color = HighDensityNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

