package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TicketStatus
import com.example.data.model.TicketUrgency
import com.example.ui.components.QuickSolutionSearchDialog
import com.example.ui.components.TicketCard
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBlueLight
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityCanvas
import com.example.ui.theme.HighDensityNavy
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.MontecarloOrange
import com.example.ui.theme.MontecarloOrangeDark
import com.example.ui.theme.StatusSolvedGreen
import com.example.ui.theme.StatusUrgentRed
import com.example.ui.viewmodel.BengkelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BengkelViewModel,
    onCreateTicketClick: () -> Unit,
    onTicketClick: (Long) -> Unit,
    onNavigateToForum: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSolutions: () -> Unit,
    onNavigateToSop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentMechanic by viewModel.currentMechanic.collectAsStateWithLifecycle()
    val tickets by viewModel.filteredTickets.collectAsStateWithLifecycle()
    val allTickets by viewModel.allTickets.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val totalBonusPaid by viewModel.totalOwnerBonusPaid.collectAsStateWithLifecycle()
    val allSolutions by viewModel.allSolutionPosts.collectAsStateWithLifecycle()


    var showQuickSearchDialog by remember { mutableStateOf(false) }

    if (showQuickSearchDialog) {
        QuickSolutionSearchDialog(
            searchQuery = searchQuery,
            searchResults = searchResults,
            onQueryChange = { viewModel.setSearchQuery(it) },
            onSelectTicket = { ticketId ->
                viewModel.selectTicket(ticketId)
                onTicketClick(ticketId)
            },
            onDismiss = { showQuickSearchDialog = false }
        )
    }

    val initials = currentMechanic.name
        .split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { "MK" }

    val activeTicketsCount = allTickets.count { it.status != TicketStatus.RESOLVED }
    val resolvedTicketsCount = allTickets.count { it.status == TicketStatus.RESOLVED }
    val urgentTicketsCount = allTickets.count { it.urgency != TicketUrgency.NORMAL }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HighDensityCanvas)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. High Density Header: Dynamic mechanic identity
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToProfile() }
                    ) {
                        Text(
                            text = currentMechanic.branchName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = HighDensityTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Halo, ${currentMechanic.name} 👋",
                                style = MaterialTheme.typography.headlineSmall,
                                color = HighDensityNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }

                    // Avatar Circle with dynamic initials
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(HighDensityBlueLight)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { onNavigateToProfile() }
                            .testTag("dashboard_profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium,
                            color = HighDensityNavy,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Performance & Ticket Metrics (4 Structured Metric Tiles)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Metric 1: Tiket Aktif
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setFilter("OPEN")
                                onNavigateToForum()
                            }
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "$activeTicketsCount",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityNavy
                            )
                            Text(
                                text = "Tiket Berjalan",
                                style = MaterialTheme.typography.labelSmall,
                                color = HighDensityTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Metric 2: Kasus Selesai
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setFilter("RESOLVED")
                                onNavigateToForum()
                            }
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "$resolvedTicketsCount",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = StatusSolvedGreen
                            )
                            Text(
                                text = "Kasus Selesai",
                                style = MaterialTheme.typography.labelSmall,
                                color = HighDensityTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Metric 3: Tiket Mogok/Urgent
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setFilter("URGENT")
                                onNavigateToForum()
                            }
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "$urgentTicketsCount",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = StatusUrgentRed
                            )
                            Text(
                                text = "Mogok / Urgent",
                                style = MaterialTheme.typography.labelSmall,
                                color = HighDensityTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Metric 4: SOP Standar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, HighDensityBorder, RoundedCornerShape(12.dp))
                            .clickable { onNavigateToSop() }
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "${viewModel.sopList.size}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityBlue
                            )
                            Text(
                                text = "Buku SOP",
                                style = MaterialTheme.typography.labelSmall,
                                color = HighDensityTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. Tombol Aksi Cepat (Quick Actions Grid)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // [+] Buat Tiket Kendala (Orange Montecarlo Button)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(108.dp)
                            .testTag("quick_action_create_ticket")
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onCreateTicketClick() },
                        colors = CardDefaults.cardColors(containerColor = MontecarloOrange),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(MontecarloOrange, MontecarloOrangeDark)
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.22f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Buat Tiket Kendala",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Input cepat & foto",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // [?] Cari Solusi Cepat (Diagnostic Cobalt Blue Button)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(108.dp)
                            .testTag("quick_action_find_solution")
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showQuickSearchDialog = true },
                        colors = CardDefaults.cardColors(containerColor = HighDensityBlue),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(HighDensityBlue, Color(0xFF003F8A))
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.22f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Cari Solusi Cepat",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "DTC & 18 cabang",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3.5 Wadah Solusi & Kasus Berhadiah Owner (Featured Card)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToSolutions() }
                    .testTag("card_wadah_solusi_dashboard"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFD97706), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "WADAH SOLUSI BERHADIAH",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF92400E)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFD97706), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "BONUS OWNER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Posting solusi kendala unik dari cabang Anda & dapatkan bayaran bonus dari Owner!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = Color(0xFF78350F)
                            ),
                            maxLines = 2
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Rp ${String.format("%,d", totalBonusPaid).replace(',', '.')}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        )
                        Text(
                            text = "${allSolutions.size} Solusi",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = Color(0xFF92400E)
                            )
                        )
                    }
                }
            }
        }

        // 4. Daftar Tiket Terbuka Header & Filter Chips
        item {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAFTAR TIKET KENDALA (18 CABANG)",
                        style = MaterialTheme.typography.labelSmall,
                        color = HighDensityNavy,
                        letterSpacing = 0.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )

                    Text(
                        text = "Lihat Semua",
                        style = MaterialTheme.typography.labelSmall,
                        color = HighDensityBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clickable { onNavigateToForum() }
                            .padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // High Density Filter Tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    val filterOptions = listOf(
                        "ALL" to "Semua (${allTickets.size})",
                        "MY_BRANCH" to "Cabang Saya (${currentMechanic.branchId})",
                        "URGENT" to "🔥 Mogok / Urgent",
                        "MESIN" to "Mesin & Koil",
                        "MATIC" to "Matic",
                        "KELISTRIKAN" to "Sensor & DTC"
                    )

                    items(filterOptions) { (key, label) ->
                        val isSelected = selectedFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) HighDensityBlueLight else Color.White
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) HighDensityBlue else HighDensityBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setFilter(key) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) HighDensityNavy else HighDensityTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Tickets List
        if (tickets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = HighDensityTextSecondary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak ada kendala dalam filter ini.",
                            style = MaterialTheme.typography.titleSmall,
                            color = HighDensityNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Semua unit di cabang ini sedang tertangani dengan baik.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HighDensityTextSecondary
                        )
                    }
                }
            }
        } else {
            items(tickets) { ticket ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    TicketCard(
                        ticket = ticket,
                        onClick = {
                            viewModel.selectTicket(ticket.id)
                            onTicketClick(ticket.id)
                        }
                    )
                }
            }
        }
    }
}
