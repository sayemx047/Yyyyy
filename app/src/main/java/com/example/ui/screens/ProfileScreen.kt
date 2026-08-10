package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseConfig
import com.example.data.UserEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: UserEntity?,
    onChangePasswordSubmit: (currentPass: String, newPass: String) -> Unit,
    onDeleteAccountSubmit: (currentPass: String) -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val firebaseStatus = remember { FirebaseConfig.checkFirebaseStatus(context) }

    var showPasswordModal by remember { mutableStateOf(false) }
    var showDeleteModal by remember { mutableStateOf(false) }

    var currentPassInput by remember { mutableStateOf("") }
    var newPassInput by remember { mutableStateOf("") }

    FrostedGlassBackground {
        Scaffold(
            topBar = {
                Surface(
                    color = Color(0x14FFFFFF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                ) {
                    TopAppBar(
                        title = { Text("PLAYER PROFILE & SETTINGS", fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 1.sp) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = TextPrimary
                        )
                    )
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // User Profile Card (Frosted Glass)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(PurplePrimary, IndigoAccent))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(currentUser?.name ?: "Gamer", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                            Text(currentUser?.email ?: "", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(color = GoldAccent.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "FF UID: ${(currentUser?.ffUid ?: "").ifBlank { "Not Set" }}",
                                    fontSize = 11.sp,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Device Telemetry Logging Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x14FFFFFF)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x20FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = PurplePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("LOGGED DEVICE TELEMETRY", fontWeight = FontWeight.Black, color = PurplePrimary, fontSize = 12.sp, letterSpacing = 1.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TelemetryRow(icon = Icons.Default.Smartphone, label = "Device Model", value = (currentUser?.deviceModel ?: "").ifBlank { "Android Smartphone" })
                        TelemetryRow(icon = Icons.Default.BatteryChargingFull, label = "Battery Status", value = (currentUser?.batteryLevel ?: "").ifBlank { "95%" })
                        TelemetryRow(icon = Icons.Default.Wifi, label = "Network Connection", value = (currentUser?.networkType ?: "").ifBlank { "Wi-Fi (Online)" })
                        TelemetryRow(icon = Icons.Default.Dns, label = "IP Address", value = (currentUser?.ipAddress ?: "").ifBlank { "192.168.1.100" })
                    }
                }

                // Firebase Config Hub Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = IndigoAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FIREBASE BACKEND HUB", fontWeight = FontWeight.Black, color = IndigoAccent, fontSize = 12.sp, letterSpacing = 1.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(firebaseStatus, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                        Text(
                            "Place `google-services.json` in `/app/` to switch live cloud database control.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Password Change Action Button
                OutlinedButton(
                    onClick = { showPasswordModal = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
                ) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CHANGE PASSWORD (REQUIRES CURRENT PASS)", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Delete Account Action Button
                OutlinedButton(
                    onClick = { showDeleteModal = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ErrorRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DELETE ACCOUNT PERMANENTLY", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Logout Button
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x20FFFFFF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = TextPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LOGOUT", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Change Password Dialog
            if (showPasswordModal) {
                AlertDialog(
                    onDismissRequest = { showPasswordModal = false },
                    title = { Text("CHANGE PASSWORD", fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 15.sp) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = currentPassInput,
                                onValueChange = { currentPassInput = it },
                                label = { Text("Current Password *", color = TextSecondary) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldAccent,
                                    unfocusedBorderColor = Color(0x2BFFFFFF),
                                    focusedLabelColor = GoldAccent
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = newPassInput,
                                onValueChange = { newPassInput = it },
                                label = { Text("New Password *", color = TextSecondary) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldAccent,
                                    unfocusedBorderColor = Color(0x2BFFFFFF),
                                    focusedLabelColor = GoldAccent
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onChangePasswordSubmit(currentPassInput, newPassInput)
                                showPasswordModal = false
                                currentPassInput = ""
                                newPassInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                        ) {
                            Text("UPDATE PASSWORD", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPasswordModal = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    },
                    containerColor = Color(0xFF1E1B2E),
                    shape = RoundedCornerShape(20.dp)
                )
            }

            // Delete Account Dialog
            if (showDeleteModal) {
                AlertDialog(
                    onDismissRequest = { showDeleteModal = false },
                    title = { Text("DELETE ACCOUNT PERMANENTLY", fontWeight = FontWeight.Black, color = ErrorRed, fontSize = 15.sp) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("This action is permanent and cannot be undone. Enter your current password to confirm deletion:", color = TextSecondary, fontSize = 12.sp)
                            OutlinedTextField(
                                value = currentPassInput,
                                onValueChange = { currentPassInput = it },
                                label = { Text("Current Password *", color = TextSecondary) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ErrorRed,
                                    unfocusedBorderColor = Color(0x2BFFFFFF),
                                    focusedLabelColor = ErrorRed
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDeleteAccountSubmit(currentPassInput)
                                showDeleteModal = false
                                currentPassInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                        ) {
                            Text("DELETE MY ACCOUNT", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteModal = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    },
                    containerColor = Color(0xFF1E1B2E),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

@Composable
private fun TelemetryRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = TextSecondary, fontSize = 12.sp)
        }
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

