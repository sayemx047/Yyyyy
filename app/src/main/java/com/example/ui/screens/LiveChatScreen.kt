package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ChatMessageEntity
import com.example.data.UserEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveChatScreen(
    currentUser: UserEntity?,
    messages: List<ChatMessageEntity>,
    messageText: String,
    selectedImageUrl: String?,
    editingMessageId: Int?,
    limitErrorAlert: String?,
    showGalleryPicker: Boolean,
    onMessageTextChange: (String) -> Unit,
    onSelectPresetImage: (String?) -> Unit,
    onToggleGalleryPicker: (Boolean) -> Unit,
    onStartEditingMessage: (ChatMessageEntity) -> Unit,
    onCancelEditing: () -> Unit,
    onDismissLimitAlert: () -> Unit,
    onSendMessage: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val presetImages = listOf(
        Pair("Match Screenshot", R.drawable.banner_esports_1786082485993),
        Pair("bKash Receipt", R.drawable.ic_esports_logo_1786082473239)
    )

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
                                Text("LIVE SUPPORT CHAT 💬", fontWeight = FontWeight.Black, fontSize = 15.sp, color = TextPrimary, letterSpacing = 1.sp)
                                Text("Limit: Max 3 messages until admin replies", fontSize = 10.sp, color = GoldAccent)
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
                    .padding(12.dp)
            ) {
                // Auto-Deletion Notice Banner (Frosted)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x20FFFFFF))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⚡ Auto Chat Cleanup: Messages older than 48 hours are automatically purged.",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chat Messages List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val isMe = msg.senderEmail.equals(currentUser?.email ?: "user", ignoreCase = true)
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (msg.isAdmin) Color(0x336366F1) else if (isMe) Color(0x33A855F7) else Color(0x18FFFFFF)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (msg.isAdmin) GoldAccent else if (isMe) PurplePrimary else Color(0x2EFFFFFF)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (msg.isAdmin) "👑 ${msg.senderName} (Admin)" else msg.senderName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (msg.isAdmin) GoldAccent else PurplePrimary
                                        )

                                        // User edit message capability
                                        if (isMe && !msg.isAdmin) {
                                            IconButton(
                                                onClick = { onStartEditingMessage(msg) },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }

                                    if (msg.text.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = msg.text, color = TextPrimary, fontSize = 13.sp)
                                    }

                                    if (msg.isEdited) {
                                        Text("(edited)", fontSize = 9.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Gallery Attachment Drawer Toggle
                if (showGalleryPicker) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2EFFFFFF))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("SELECT ATTACHMENT FROM GALLERY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(presetImages) { item ->
                                    Card(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clickable {
                                                onSelectPresetImage(item.first)
                                            },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = item.second),
                                            contentDescription = item.first,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Input Bar
                if (editingMessageId != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✏️ Editing message...", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onCancelEditing) {
                            Text("Cancel Edit", color = ErrorRed, fontSize = 11.sp)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onToggleGalleryPicker(!showGalleryPicker) }) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = "Attach Gallery Image",
                            tint = if (selectedImageUrl != null) SuccessGreen else TextSecondary
                        )
                    }

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = onMessageTextChange,
                        placeholder = { Text("Type message...", fontSize = 13.sp, color = TextSecondary) },
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
                        onClick = onSendMessage,
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(listOf(PurplePrimary, IndigoAccent)),
                                shape = CircleShape
                            )
                            .size(42.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Limit Error Alert
            if (limitErrorAlert != null) {
                AlertDialog(
                    onDismissRequest = onDismissLimitAlert,
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = WarningYellow)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("MESSAGE LIMIT REACHED", fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 15.sp)
                        }
                    },
                    text = {
                        Text(limitErrorAlert, color = TextSecondary, fontSize = 13.sp)
                    },
                    confirmButton = {
                        Button(onClick = onDismissLimitAlert, colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)) {
                            Text("UNDERSTOOD", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = Color(0xFF1E1B2E),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

