package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    pendingTransactions: List<WalletTransactionEntity>,
    allUsers: List<UserEntity>,
    allTournaments: List<TournamentEntity>,
    noticeTitle: String,
    noticeContent: String,
    noticeTargetEmail: String,
    statusFeedback: String?,
    onNoticeTitleChange: (String) -> Unit,
    onNoticeContentChange: (String) -> Unit,
    onNoticeTargetChange: (String) -> Unit,
    onApproveTx: (Int) -> Unit,
    onRejectTx: (Int) -> Unit,
    onPublishNotice: () -> Unit,
    onUpdateRoomDetails: (tournamentId: Int, roomId: String, roomPass: String) -> Unit,
    onClearFeedback: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: TxIDs, 1: Room IDs, 2: Notices, 3: User Telemetry

    FrostedGlassBackground {
        Scaffold(
            topBar = {
                Surface(
                    color = Color(0x14FFFFFF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                ) {
                    TopAppBar(
                        title = { Text("👑 ADMIN CONTROL PANEL", fontWeight = FontWeight.Black, fontSize = 15.sp, color = GoldAccent, letterSpacing = 1.sp) },
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
            ) {
                // Tabs Row (Frosted)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0x1AFFFFFF),
                    contentColor = GoldAccent
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("bKash (${pendingTransactions.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp), color = if (selectedTab == 0) GoldAccent else TextSecondary)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Room IDs", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp), color = if (selectedTab == 1) GoldAccent else TextSecondary)
                    }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Text("Notices", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp), color = if (selectedTab == 2) GoldAccent else TextSecondary)
                    }
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                        Text("Telemetry", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp), color = if (selectedTab == 3) GoldAccent else TextSecondary)
                    }
                }

                if (statusFeedback != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(statusFeedback, color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = onClearFeedback, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = SuccessGreen)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        // bKash TxID Verification Queue
                        if (pendingTransactions.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No pending bKash deposit requests.", color = TextSecondary, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(pendingTransactions) { tx ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("User: ${tx.userEmail}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                            Text("TxID: ${tx.transactionId}", color = GoldAccent, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                            Text("Amount: ৳ ${tx.amount.toInt()} BDT", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = { onApproveTx(tx.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("APPROVE & CREDIT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                                Button(
                                                    onClick = { onRejectTx(tx.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("REJECT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Room ID & Password Distributor
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(allTournaments) { t ->
                                var roomIdState by remember(t.id) { mutableStateOf(t.roomId) }
                                var roomPassState by remember(t.id) { mutableStateOf(t.roomPassword) }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2EFFFFFF))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(t.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = roomIdState,
                                                onValueChange = { roomIdState = it },
                                                label = { Text("Room ID", color = TextSecondary) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = PurplePrimary,
                                                    unfocusedBorderColor = Color(0x2BFFFFFF)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            OutlinedTextField(
                                                value = roomPassState,
                                                onValueChange = { roomPassState = it },
                                                label = { Text("Password", color = TextSecondary) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = PurplePrimary,
                                                    unfocusedBorderColor = Color(0x2BFFFFFF)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { onUpdateRoomDetails(t.id, roomIdState, roomPassState) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                            contentPadding = PaddingValues(),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        Brush.horizontalGradient(listOf(PurplePrimary, IndigoAccent)),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("PUBLISH ROOM DETAILS TO PARTICIPANTS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Notice Publisher
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2EFFFFFF))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("PUBLISH NOTICE TO PLAYERS", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = noticeTitle,
                                    onValueChange = onNoticeTitleChange,
                                    label = { Text("Notice Title", color = TextSecondary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PurplePrimary,
                                        unfocusedBorderColor = Color(0x2BFFFFFF)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = noticeContent,
                                    onValueChange = onNoticeContentChange,
                                    label = { Text("Notice Content", color = TextSecondary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PurplePrimary,
                                        unfocusedBorderColor = Color(0x2BFFFFFF)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = noticeTargetEmail,
                                    onValueChange = onNoticeTargetChange,
                                    label = { Text("Target Email ('ALL' or specific email)", color = TextSecondary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PurplePrimary,
                                        unfocusedBorderColor = Color(0x2BFFFFFF)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onPublishNotice,
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("PUBLISH NOTICE", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    3 -> {
                        // Logged User Device Telemetry Viewer
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(allUsers) { u ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x20FFFFFF))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("${u.name} (${u.email})", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                        Text("FF UID: ${u.ffUid}", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("📱 Model: ${u.deviceModel.ifBlank { "Unknown Android" }}", fontSize = 11.sp, color = TextSecondary)
                                        Text("🔋 Battery: ${u.batteryLevel.ifBlank { "N/A" }}", fontSize = 11.sp, color = TextSecondary)
                                        Text("🌐 Network: ${u.networkType.ifBlank { "Online" }}", fontSize = 11.sp, color = TextSecondary)
                                        Text("📡 IP Address: ${u.ipAddress.ifBlank { "192.168.1.1" }}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

