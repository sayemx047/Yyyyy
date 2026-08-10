package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessageEntity
import com.example.data.TournamentEntity
import com.example.data.UserEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentPrivateInfoScreen(
    tournament: TournamentEntity,
    currentUser: UserEntity?,
    chatMessages: List<ChatMessageEntity>,
    chatMessageText: String,
    onChatMessageChange: (String) -> Unit,
    onSendChatMessage: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf<String?>(null) }

    FrostedGlassBackground {
        Scaffold(
            topBar = {
                Surface(
                    color = Color(0x14FFFFFF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                ) {
                    TopAppBar(
                        title = {
                            Column {
                                Text("PRIVATE TOURNAMENT INFO", fontSize = 14.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 1.sp)
                                Text(tournament.title, fontSize = 11.sp, color = PurplePrimary)
                            }
                        },
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
                // Room ID & Password Banner Card (Frosted with Gold Border)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x221E1B2E)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CUSTOM ROOM ACCESS", fontWeight = FontWeight.Black, color = GoldAccent, fontSize = 13.sp, letterSpacing = 1.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("ROOM ID", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    text = tournament.roomId.ifBlank { "Unassigned Yet" },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            }
                            if (tournament.roomId.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(tournament.roomId))
                                        copiedNotice = "Room ID Copied!"
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Room ID", tint = PurplePrimary)
                                }
                            }

                            Column {
                                Text("ROOM PASSWORD", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    text = tournament.roomPassword.ifBlank { "Unassigned Yet" },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            }
                            if (tournament.roomPassword.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(tournament.roomPassword))
                                        copiedNotice = "Password Copied!"
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Room Password", tint = PurplePrimary)
                                }
                            }
                        }

                        if (copiedNotice != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(copiedNotice!!, color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tournament Rules Card (Frosted)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x20FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("TOURNAMENT RULES", fontWeight = FontWeight.Black, color = PurplePrimary, fontSize = 11.sp, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(tournament.rules, color = TextSecondary, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Participant-Admin Private Live Chat Section
                Text("PARTICIPANT - ADMIN LIVE CHAT", fontWeight = FontWeight.Black, color = GoldAccent, fontSize = 12.sp, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0x18FFFFFF)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x20FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(chatMessages) { msg ->
                                val isMe = msg.senderEmail.equals(currentUser?.email, ignoreCase = true)
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Surface(
                                        color = if (msg.isAdmin) Color(0x336366F1) else if (isMe) Color(0x33A855F7) else Color(0x22FFFFFF),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (msg.isAdmin) GoldAccent else if (isMe) PurplePrimary else Color(0x20FFFFFF)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = if (msg.isAdmin) "👑 ${msg.senderName} (Admin)" else msg.senderName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (msg.isAdmin) GoldAccent else PurplePrimary
                                            )
                                            Text(text = msg.text, color = TextPrimary, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = chatMessageText,
                                onValueChange = onChatMessageChange,
                                placeholder = { Text("Chat with admin...", fontSize = 12.sp, color = TextSecondary) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = Color(0x2BFFFFFF),
                                    focusedContainerColor = Color(0x0DFFFFFF),
                                    unfocusedContainerColor = Color(0x08FFFFFF)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onSendChatMessage,
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(listOf(PurplePrimary, IndigoAccent)),
                                        shape = CircleShape
                                    )
                                    .size(40.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

