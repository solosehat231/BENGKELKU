package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityCanvas
import com.example.ui.theme.HighDensityNavy
import com.example.ui.theme.HighDensityTextPrimary
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.theme.MontecarloOrange
import com.example.ui.theme.MontecarloOrangeDark
import com.example.ui.viewmodel.BengkelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: BengkelViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val branches = viewModel.branches18

    var selectedRole by remember { mutableStateOf(UserRole.MECHANIC) }
    var mechanicName by remember { mutableStateOf("Budi Santoso") }
    var selectedBranchId by remember { mutableIntStateOf(1) }
    var branchDropdownExpanded by remember { mutableStateOf(false) }

    var adminPin by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }

    val quickPresetMechanics = listOf(
        Triple("Budi Santoso", 1, UserRole.MECHANIC),
        Triple("Aulia Prawira Negara", 2, UserRole.MECHANIC),
        Triple("Pak Hendra (Admin / Owner)", 1, UserRole.ADMIN_OWNER)
    )

    val currentBranch = branches.find { it.id == selectedBranchId } ?: branches[0]

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("login_screen"),
        containerColor = HighDensityCanvas
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // App Brand Logo (Monte Carlo Group)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_group_2),
                    contentDescription = "Logo Monte Carlo Group",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "BENGKELKU",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = HighDensityNavy
            )

            Text(
                text = "Portal Mekanik Seluruh Cabang",
                style = MaterialTheme.typography.bodyMedium,
                color = HighDensityTextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Role Selector Tabs (2 Roles: Mekanik & Admin/Owner)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "PILIH PERAN / HAK AKSES:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityNavy,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.values().forEach { role ->
                            val isSelected = selectedRole == role
                            val (roleIcon, roleTitle, roleDesc) = when (role) {
                                UserRole.MECHANIC -> Triple(Icons.Default.Engineering, "Mekanik Cabang", "Input & Solusi Kasus")
                                UserRole.ADMIN_OWNER -> Triple(Icons.Default.AdminPanelSettings, "Admin / Owner", "Tentukan Besaran Reward")
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedRole = role
                                        pinError = null
                                        adminPin = ""
                                        if (role == UserRole.ADMIN_OWNER) {
                                            mechanicName = "Pak Hendra (Owner)"
                                        } else {
                                            mechanicName = "Budi Santoso"
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(role.badgeColorHex) else HighDensityCanvas,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = roleIcon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else HighDensityNavy,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = roleTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else HighDensityNavy,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = roleDesc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else HighDensityTextSecondary,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Login Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (selectedRole) {
                                UserRole.MECHANIC -> Icons.Default.Engineering
                                UserRole.ADMIN_OWNER -> Icons.Default.Security
                            },
                            contentDescription = null,
                            tint = Color(selectedRole.badgeColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FORM LOGIN ${selectedRole.label.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = HighDensityNavy,
                            letterSpacing = 0.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Name Input
                    OutlinedTextField(
                        value = mechanicName,
                        onValueChange = { mechanicName = it },
                        label = {
                            Text(
                                if (selectedRole == UserRole.ADMIN_OWNER) "Nama Admin / Owner *"
                                else "Nama Lengkap Mekanik *"
                            )
                        },
                        placeholder = { Text(if (selectedRole == UserRole.ADMIN_OWNER) "Contoh: Pak Hendra" else "Contoh: Budi Santoso") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = HighDensityBlue)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_input_name"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityBlue,
                            unfocusedBorderColor = HighDensityBorder,
                            focusedTextColor = HighDensityTextPrimary,
                            unfocusedTextColor = HighDensityTextPrimary,
                            focusedContainerColor = HighDensityCanvas,
                            unfocusedContainerColor = HighDensityCanvas
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Branch Dropdown Selector (18 Branches)
                    ExposedDropdownMenuBox(
                        expanded = branchDropdownExpanded,
                        onExpandedChange = { branchDropdownExpanded = !branchDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = currentBranch.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (selectedRole == UserRole.ADMIN_OWNER) "Kantor Cabang / Pusat *" else "Pilih Cabang Penugasan *") },
                            leadingIcon = {
                                Icon(Icons.Default.Business, contentDescription = null, tint = MontecarloOrange)
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("login_select_branch"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HighDensityBlue,
                                unfocusedBorderColor = HighDensityBorder,
                                focusedTextColor = HighDensityTextPrimary,
                                unfocusedTextColor = HighDensityTextPrimary,
                                focusedContainerColor = HighDensityCanvas,
                                unfocusedContainerColor = HighDensityCanvas
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = branchDropdownExpanded,
                            onDismissRequest = { branchDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            branches.forEach { b ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = b.name,
                                                fontWeight = FontWeight.Bold,
                                                color = HighDensityNavy
                                            )
                                            Text(
                                                text = "${b.city} • Alamat: ${b.address}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = HighDensityTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedBranchId = b.id
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

                    // PIN Authorization for Admin / Owner
                    AnimatedVisibility(visible = selectedRole == UserRole.ADMIN_OWNER) {
                        Column {
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = adminPin,
                                onValueChange = {
                                    if (it.length <= 6) {
                                        adminPin = it
                                        pinError = null
                                    }
                                },
                                label = { Text("PIN Otorisasi Owner/Admin *") },
                                placeholder = { Text("PIN Default: 9988") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(selectedRole.badgeColorHex))
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                        Icon(
                                            imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = HighDensityTextSecondary
                                        )
                                    }
                                },
                                visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                isError = pinError != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_input_pin"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(selectedRole.badgeColorHex),
                                    unfocusedBorderColor = HighDensityBorder,
                                    focusedTextColor = HighDensityTextPrimary,
                                    unfocusedTextColor = HighDensityTextPrimary,
                                    focusedContainerColor = HighDensityCanvas,
                                    unfocusedContainerColor = HighDensityCanvas
                                )
                            )

                            if (pinError != null) {
                                Text(
                                    text = pinError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 6.dp, top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🔐 Hak Akses Admin / Owner: Menentukan besaran nominal reward untuk postingan solusi mekanik. (Nominal bonus diberikan secara tunai, PIN Demo: 9988)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF92400E),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Login Button
                    Button(
                        onClick = {
                            if (selectedRole == UserRole.ADMIN_OWNER) {
                                val isValid = viewModel.validateAdminPin(selectedRole, adminPin.trim())
                                if (!isValid) {
                                    pinError = "PIN Admin salah! Gunakan PIN: 9988"
                                    return@Button
                                }
                            }

                            viewModel.login(
                                name = mechanicName,
                                branchId = selectedBranchId,
                                role = selectedRole
                            )
                            onLoginSuccess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(selectedRole.badgeColorHex),
                            contentColor = Color.White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Masuk Sebagai ${selectedRole.label}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Preset 1-Click Login (Demo Roles)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = MontecarloOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AKUN CEPAT (MULTI-ROLE DEMO):",
                            style = MaterialTheme.typography.labelSmall,
                            color = HighDensityNavy,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    quickPresetMechanics.forEach { (name, branchId, role) ->
                        val br = branches.find { it.id == branchId }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(HighDensityCanvas)
                                .border(1.dp, HighDensityBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedRole = role
                                    mechanicName = name
                                    selectedBranchId = branchId
                                    viewModel.login(
                                        name = name,
                                        branchId = branchId,
                                        role = role
                                    )
                                    onLoginSuccess()
                                }
                                .padding(10.dp)
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
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(role.badgeColorHex).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (role) {
                                                UserRole.ADMIN_OWNER -> Icons.Default.WorkspacePremium
                                                UserRole.MECHANIC -> Icons.Default.Engineering
                                            },
                                            contentDescription = null,
                                            tint = Color(role.badgeColorHex),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = HighDensityNavy
                                            )
                                        }
                                        Text(
                                            text = "${role.label} • ${br?.name ?: ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = HighDensityTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Surface(
                                    color = Color(role.badgeColorHex),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Login ➔",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
