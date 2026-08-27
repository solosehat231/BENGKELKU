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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.TicketCard
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBlueLight
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityCanvas
import com.example.ui.theme.HighDensityNavy
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.MontecarloOrange
import com.example.ui.viewmodel.BengkelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    viewModel: BengkelViewModel,
    onCreateTicketClick: () -> Unit,
    onTicketClick: (Long) -> Unit,
    onNavigateToSolutions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tickets by viewModel.filteredTickets.collectAsStateWithLifecycle()
    val allTickets by viewModel.allTickets.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val currentMechanic by viewModel.currentMechanic.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Forum Kendala 18 Cabang",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityNavy
                            )
                            Text(
                                text = "${currentMechanic.branchName} • Total ${allTickets.size} Tiket Terdaftar",
                                style = MaterialTheme.typography.bodySmall,
                                color = HighDensityTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    },
                    actions = {
                        androidx.compose.material3.TextButton(onClick = onNavigateToSolutions) {
                            Text("💰 Bank Solusi", fontWeight = FontWeight.Bold, color = MontecarloOrange)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                HorizontalDivider(color = HighDensityBorder, thickness = 1.dp)
            }
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTicketClick,
                containerColor = MontecarloOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("forum_fab_create_ticket")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Buat Kendala")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HighDensityCanvas)
                .testTag("forum_screen")
        ) {
            // Search Input
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Cari plat, jenis mobil, kode DTC...", fontSize = 13.sp, color = HighDensityTextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HighDensityBlue) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forum_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityBlue,
                        unfocusedBorderColor = HighDensityBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = HighDensityTextPrimary,
                        unfocusedTextColor = HighDensityTextPrimary
                    ),
                    singleLine = true
                )
            }

            // Filter Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filterList = listOf(
                    "ALL" to "Semua (${allTickets.size})",
                    "OPEN" to "Terbuka",
                    "MY_BRANCH" to "Cabang Saya (${currentMechanic.branchId})",
                    "URGENT" to "🔥 Urgent",
                    "MESIN" to "Mesin",
                    "MATIC" to "Matic",
                    "KELISTRIKAN" to "Kelistrikan",
                    "RESOLVED" to "✅ Terpecahkan"
                )

                items(filterList) { (key, label) ->
                    val isSelected = selectedFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) HighDensityBlueLight else Color.White
                            )
                            .border(
                                1.dp,
                                if (isSelected) HighDensityBlueLight else HighDensityBorder,
                                RoundedCornerShape(12.dp)
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

            Spacer(modifier = Modifier.height(6.dp))

            // Ticket Cards
            if (tickets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                            text = "Tidak ada tiket ditemukan untuk filter ini.",
                            style = MaterialTheme.typography.titleMedium,
                            color = HighDensityNavy,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Coba ubah kata kunci pencarian atau reset filter ke Semua Tiket.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HighDensityTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        androidx.compose.material3.Button(
                            onClick = {
                                viewModel.setFilter("ALL")
                                viewModel.setSearchQuery("")
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = HighDensityBlue
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Tampilkan Semua Tiket (18 Cabang)")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tickets) { ticket ->
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
}

