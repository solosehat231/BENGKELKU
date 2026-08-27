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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SolutionPostEntity
import com.example.data.model.UserRole
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
fun ProfileScheduleScreen(
    viewModel: BengkelViewModel,
    onLogoutClick: () -> Unit = {},
    onNavigateToSolutions: () -> Unit = {},
    onNavigateToForum: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentMechanic by viewModel.currentMechanic.collectAsStateWithLifecycle()
    val branches18 = viewModel.branches18
    val allSolutionPosts by viewModel.allSolutionPosts.collectAsStateWithLifecycle()
    val totalOwnerBonusPaid by viewModel.totalOwnerBonusPaid.collectAsStateWithLifecycle()

    val rewardedPosts = remember(allSolutionPosts) {
        allSolutionPosts.filter { it.isOwnerRewarded }.sortedByDescending { it.ownerRewardedAt ?: it.createdAt }
    }

    val isOwner = currentMechanic.role == UserRole.ADMIN_OWNER

    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(currentMechanic.name) }
    var editBranchId by remember { mutableIntStateOf(currentMechanic.branchId) }
    var branchDropdownExpanded by remember { mutableStateOf(false) }

    val initials = currentMechanic.name
        .split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifEmpty { if (isOwner) "OW" else "MK" }

    val currentBranch = branches18.find { it.id == editBranchId } ?: branches18[0]

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isOwner) "Profil Manajemen Owner" else "Profil Mekanik & Pengaturan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityNavy
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                HorizontalDivider(color = HighDensityBorder, thickness = 1.dp)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HighDensityCanvas)
                .testTag("profile_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Identity Card (Mechanic or Owner)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (isOwner) Color(0xFFFEF3C7) else HighDensityBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isOwner) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(30.dp)
                                    )
                                } else {
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = HighDensityNavy,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentMechanic.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = HighDensityNavy
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Color(currentMechanic.role.badgeColorHex),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    color = Color(currentMechanic.role.badgeColorHex).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "🛡️ ${currentMechanic.role.label}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(currentMechanic.role.badgeColorHex),
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isOwner) "📍 Kantor Pusat & 18 Cabang Montecarlo" else "📍 ${currentMechanic.branchName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MontecarloOrange,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats row (Mechanic vs Owner)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isOwner) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(HighDensityCanvas, RoundedCornerShape(10.dp))
                                        .border(1.dp, HighDensityBorder, RoundedCornerShape(10.dp))
                                        .clickable { onNavigateToSolutions() }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${allSolutionPosts.size} Kasus",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = HighDensityBlue
                                        )
                                        Text(
                                            text = "Bank Solusi Cabang",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HighDensityTextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(HighDensityCanvas, RoundedCornerShape(10.dp))
                                        .border(1.dp, HighDensityBorder, RoundedCornerShape(10.dp))
                                        .clickable { onNavigateToSolutions() }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${rewardedPosts.size} Mekanik",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                        Text(
                                            text = "Penerima Reward",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HighDensityTextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(HighDensityCanvas, RoundedCornerShape(10.dp))
                                        .border(1.dp, HighDensityBorder, RoundedCornerShape(10.dp))
                                        .clickable { onNavigateToForum() }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "18 Cabang",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusSolvedGreen
                                        )
                                        Text(
                                            text = "Jaringan Montecarlo",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HighDensityTextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(HighDensityCanvas, RoundedCornerShape(10.dp))
                                        .border(1.dp, HighDensityBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.setFilter("RESOLVED")
                                            onNavigateToForum()
                                        }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${currentMechanic.solvedTicketsCount} Kasus",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = HighDensityBlue
                                        )
                                        Text(
                                            text = "Terselesaikan",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HighDensityTextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(HighDensityCanvas, RoundedCornerShape(10.dp))
                                        .border(1.dp, HighDensityBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.setFilter("MY_BRANCH")
                                            onNavigateToForum()
                                        }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${currentMechanic.sharedSolutionsCount} Solusi",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusSolvedGreen
                                        )
                                        Text(
                                            text = "Diskusi Cabang",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HighDensityTextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(HighDensityCanvas, RoundedCornerShape(10.dp))
                                        .border(1.dp, HighDensityBorder, RoundedCornerShape(10.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = MontecarloOrange,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "4.9",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MontecarloOrange
                                            )
                                        }
                                        Text(
                                            text = "Reputasi Mekanik",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HighDensityTextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isOwner) {
                            // Total Akumulasi Reward yang Ditetapkan Owner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFFD97706), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.MonetizationOn,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Total Reward Ditetapkan Owner",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF92400E)
                                                )
                                            )
                                            Text(
                                                text = "Akumulasi dana tunai untuk mekanik cabang",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF78350F)
                                                )
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Rp ${String.format("%,d", totalOwnerBonusPaid).replace(',', '.')}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF047857)
                                        )
                                    )
                                }
                            }
                        } else {
                            // Bonus Dari Owner Bengkel Card (Khusus Mekanik)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFFD97706), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Verified,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Reward Dari Owner (Tunai)",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF92400E)
                                                )
                                            )
                                            Text(
                                                text = "Apresiasi postingan solusi (diserahkan tunai)",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF78350F)
                                                )
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Rp ${String.format("%,d", currentMechanic.totalBonusEarned).replace(',', '.')}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF047857)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // If logged in as Owner: show Section "Daftar Akumulasi Mekanik Yang Mendapatkan Reward"
            if (isOwner) {
                item {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DAFTAR AKUMULASI MEKANIK PENERIMA REWARD",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityNavy
                                ),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Rekapitulasi mekanik 18 cabang yang berhak menerima penyerahan uang tunai:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = HighDensityTextSecondary
                            )
                        )
                    }
                }

                if (rewardedPosts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Belum Ada Penetapan Reward",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityNavy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Anda dapat menetapkan besaran reward tunai pada postingan solusi di Forum Solusi.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HighDensityTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(rewardedPosts, key = { it.id }) { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFEF3C7)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Engineering,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = post.mechanicName,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = HighDensityNavy
                                                )
                                            )
                                            Text(
                                                text = "📍 ${post.branchName}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 11.sp,
                                                    color = MontecarloOrange,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }

                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Payments,
                                                contentDescription = null,
                                                tint = Color(0xFF047857),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "+Rp ${String.format("%,d", post.rewardAmount).replace(',', '.')}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF047857),
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Solusi Kasus Info
                                Surface(
                                    color = HighDensityCanvas,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = HighDensityBlueLight,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = post.category,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = HighDensityNavy
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${post.vehicleBrand} ${post.vehicleModel} (${post.year})",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    color = HighDensityTextSecondary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = post.title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = HighDensityNavy
                                            ),
                                            maxLines = 2
                                        )
                                        if (post.ownerNote.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Surface(
                                                color = Color(0xFFFEF3C7),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "💬 Catatan Owner: \"${post.ownerNote}\"",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF92400E)
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF047857),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Berhak Diserahkan Tunai",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF047857),
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }

                                    Surface(
                                        color = HighDensityBlueLight,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.clickable {
                                            viewModel.selectSolutionPost(post.id)
                                            onNavigateToSolutions()
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Buka Solusi",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = HighDensityBlue,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = null,
                                                tint = HighDensityBlue,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Form Edit Profil Mekanik

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOwner) "PENGATURAN IDENTITAS OWNER" else "PENGATURAN IDENTITAS MEKANIK",
                                style = MaterialTheme.typography.labelSmall,
                                color = HighDensityNavy,
                                letterSpacing = 0.5.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (!isEditing) {
                                OutlinedButton(
                                    onClick = {
                                        editName = currentMechanic.name
                                        editBranchId = currentMechanic.branchId
                                        isEditing = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("edit_profile_button")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ubah", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (isEditing) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Name input
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Nama Lengkap Mekanik") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = HighDensityBlue) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_edit_name"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HighDensityBlue,
                                    unfocusedBorderColor = HighDensityBorder,
                                    focusedTextColor = HighDensityTextPrimary,
                                    unfocusedTextColor = HighDensityTextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Branch Selector
                            ExposedDropdownMenuBox(
                                expanded = branchDropdownExpanded,
                                onExpandedChange = { branchDropdownExpanded = !branchDropdownExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = currentBranch.name,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Cabang Bertugas") },
                                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = MontecarloOrange) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("profile_edit_branch"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = HighDensityBlue,
                                        unfocusedBorderColor = HighDensityBorder,
                                        focusedTextColor = HighDensityTextPrimary,
                                        unfocusedTextColor = HighDensityTextPrimary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = branchDropdownExpanded,
                                    onDismissRequest = { branchDropdownExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    branches18.forEach { b ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(b.name, fontWeight = FontWeight.Bold, color = HighDensityNavy)
                                                    Text(
                                                        text = "${b.city} • Alamat: ${b.address}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = HighDensityTextSecondary,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            },
                                            onClick = {
                                                editBranchId = b.id
                                                branchDropdownExpanded = false
                                            },
                                            colors = MenuDefaults.itemColors(
                                                textColor = HighDensityNavy
                                            ),
                                            modifier = Modifier.background(Color.White)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { isEditing = false },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Batal")
                                }

                                Button(
                                    onClick = {
                                        viewModel.updateProfile(
                                            name = editName,
                                            branchId = editBranchId
                                        )
                                        isEditing = false
                                    },
                                    modifier = Modifier.weight(1f).testTag("save_profile_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HighDensityBlue,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Simpan", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Tombol Logout / Ganti Akun Mekanik
            item {
                Button(
                    onClick = {
                        viewModel.logout()
                        onLogoutClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("logout_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFEE2E2),
                        contentColor = StatusUrgentRed
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = StatusUrgentRed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ganti Mekanik / Keluar (Logout)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = StatusUrgentRed
                        )
                    }
                }
            }

            // 4. Hotline & Kontak Kepala Bengkel 18 Cabang
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "KONTAK KEPALA MEKANIK & BENGKEL 18 CABANG:",
                    style = MaterialTheme.typography.labelSmall,
                    color = HighDensityNavy,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(branches18) { branch ->
                val isMyBranch = branch.id == currentMechanic.branchId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMyBranch) Color(0xFFF0FDF4) else Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isMyBranch) StatusSolvedGreen else HighDensityBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = branch.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityNavy
                                )
                                if (isMyBranch) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(StatusSolvedGreen, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "CABANG ANDA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Penanggung Jawab: ${branch.pic}",
                                style = MaterialTheme.typography.bodySmall,
                                color = HighDensityTextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = branch.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = HighDensityTextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(HighDensityBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = HighDensityBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
