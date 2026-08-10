package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserEntity
import com.example.data.WalletTransactionEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    currentUser: UserEntity?,
    transactions: List<WalletTransactionEntity>,
    txIdInput: String,
    amountInput: String,
    statusMessage: String?,
    isLoading: Boolean,
    onTxIdChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onSubmitRecharge: () -> Unit,
    onClearStatus: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf(false) }

    val bkashNumber = "01789495251"

    FrostedGlassBackground {
        Scaffold(
            topBar = {
                Surface(
                    color = Color(0x14FFFFFF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                ) {
                    TopAppBar(
                        title = { Text("BKASH WALLET & RECHARGE", fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 1.sp) },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Balance Card (Frosted Glass)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0x18FFFFFF)),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("CURRENT WALLET BALANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.5.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "৳ ${String.format("%.2f", currentUser?.walletBalance ?: 0.0)} BDT",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = SuccessGreen
                            )
                        }
                    }
                }

                // bKash Cash In Guide Card (Frosted Glass Pink accent)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BkashPink.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = BkashPink,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("bKash", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Official Recharge Number", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(bkashNumber))
                                        copiedNotice = true
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0x14FFFFFF)),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BkashPink)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Send Money / Cash Out to:", fontSize = 10.sp, color = TextSecondary)
                                        Text(bkashNumber, fontSize = 20.sp, fontWeight = FontWeight.Black, color = BkashPink)
                                    }
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(bkashNumber))
                                            copiedNotice = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BkashPink),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (copiedNotice) "COPIED!" else "COPY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "1. Send Money via bKash to 01789495251.\n2. Copy the Transaction ID (TxID) from your bKash SMS.\n3. Enter the TxID and Amount below to submit for instant admin verification.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Recharge Form Card (Frosted Container)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2EFFFFFF))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("SUBMIT TRANSACTION ID", fontWeight = FontWeight.Black, color = PurplePrimary, fontSize = 13.sp, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = txIdInput,
                                onValueChange = onTxIdChange,
                                label = { Text("bKash Transaction ID (TxID)", color = TextSecondary) },
                                leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null, tint = PurplePrimary) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurplePrimary,
                                    unfocusedBorderColor = Color(0x2BFFFFFF),
                                    focusedLabelColor = PurplePrimary,
                                    focusedContainerColor = Color(0x0DFFFFFF),
                                    unfocusedContainerColor = Color(0x08FFFFFF)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = amountInput,
                                onValueChange = onAmountChange,
                                label = { Text("Recharge Amount (BDT)", color = TextSecondary) },
                                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = SuccessGreen) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SuccessGreen,
                                    unfocusedBorderColor = Color(0x2BFFFFFF),
                                    focusedLabelColor = SuccessGreen,
                                    focusedContainerColor = Color(0x0DFFFFFF),
                                    unfocusedContainerColor = Color(0x08FFFFFF)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onSubmitRecharge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BkashPink),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !isLoading && txIdInput.isNotBlank()
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SUBMIT FOR VERIFICATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            if (statusMessage != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = statusMessage,
                                    color = if (statusMessage.contains("submitted")) SuccessGreen else ErrorRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Transaction History Title
                item {
                    Text("MY RECHARGE HISTORY", fontWeight = FontWeight.Black, color = TextSecondary, fontSize = 11.sp, letterSpacing = 1.5.sp)
                }

                // Transaction List
                if (transactions.isEmpty()) {
                    item {
                        Text("No wallet transactions yet.", color = TextSecondary, fontSize = 12.sp)
                    }
                } else {
                    items(transactions) { tx ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0x14FFFFFF)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x20FFFFFF))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("TxID: ${tx.transactionId}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                    Text("bKash 01789495251", color = TextSecondary, fontSize = 11.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("৳ ${tx.amount.toInt()} BDT", fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 14.sp)
                                    Surface(
                                        color = when (tx.status) {
                                            "APPROVED" -> SuccessGreen.copy(alpha = 0.2f)
                                            "REJECTED" -> ErrorRed.copy(alpha = 0.2f)
                                            else -> GoldAccent.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = tx.status,
                                            color = when (tx.status) {
                                                "APPROVED" -> SuccessGreen
                                                "REJECTED" -> ErrorRed
                                                else -> GoldAccent
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
}

