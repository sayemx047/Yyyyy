package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RegistrationEntity
import com.example.data.TournamentEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentRegistrationDialog(
    tournament: TournamentEntity,
    userEmail: String,
    initialFfUid: String = "",
    initialWhatsapp: String = "",
    onDismiss: () -> Unit,
    onSubmitRegistration: (
        ffUid: String,
        firstName: String,
        lastName: String,
        squadName: String,
        player1: String,
        player2: String,
        player3: String,
        player4: String,
        whatsapp: String
    ) -> Unit
) {
    var ffUid by remember { mutableStateOf(initialFfUid) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var squadName by remember { mutableStateOf("") }
    var player1 by remember { mutableStateOf("") }
    var player2 by remember { mutableStateOf("") }
    var player3 by remember { mutableStateOf("") }
    var player4 by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf(initialWhatsapp) }

    val isSquad = tournament.gameMode.equals("SQUAD", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onSubmitRegistration(
                        ffUid, firstName, lastName, squadName,
                        player1, player2, player3, player4, whatsapp
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                enabled = ffUid.isNotBlank() && firstName.isNotBlank() && (!isSquad || squadName.isNotBlank())
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(listOf(PurplePrimary, IndigoAccent)),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("CONFIRM & PAY (৳ ${tournament.entryFee.toInt()} BDT)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AppRegistration, contentDescription = null, tint = PurplePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("TOURNAMENT REGISTRATION", fontSize = 14.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = 1.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Event: ${tournament.title}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )

                // Mandatory FF UID
                OutlinedTextField(
                    value = ffUid,
                    onValueChange = { ffUid = it },
                    label = { Text("Free Fire Player UID *", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = Color(0x2BFFFFFF),
                        focusedLabelColor = PurplePrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name *", color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = Color(0x2BFFFFFF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name", color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = Color(0x2BFFFFFF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (isSquad) {
                    HorizontalDivider(color = Color(0x20FFFFFF), modifier = Modifier.padding(vertical = 4.dp))
                    Text("SQUAD / TEAM DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)

                    OutlinedTextField(
                        value = squadName,
                        onValueChange = { squadName = it },
                        label = { Text("Squad Name *", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color(0x2BFFFFFF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = player1,
                        onValueChange = { player1 = it },
                        label = { Text("Player 1 Username (Captain)", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PurplePrimary),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = player2,
                        onValueChange = { player2 = it },
                        label = { Text("Player 2 Username", color = TextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = player3,
                        onValueChange = { player3 = it },
                        label = { Text("Player 3 Username", color = TextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = player4,
                        onValueChange = { player4 = it },
                        label = { Text("Player 4 Username", color = TextSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp Contact (Optional)", color = TextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = Color(0xFF1E1B2E),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun InsufficientCreditDialog(
    requiredAmount: Double,
    currentBalance: Double,
    onDismiss: () -> Unit,
    onRechargeClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("INSUFFICIENT BALANCE", fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 15.sp)
            }
        },
        text = {
            Column {
                Text(
                    text = "Not enough credit. Wanna recharge?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GoldAccent
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Current Balance: ৳ ${String.format("%.1f", currentBalance)} BDT", color = TextSecondary, fontSize = 13.sp)
                Text("Entry Fee Required: ৳ ${requiredAmount.toInt()} BDT", color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onRechargeClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BkashPink),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("RECHARGE VIA BKASH", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = Color(0xFF1E1B2E),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun RegistrationTicketDialog(
    registration: RegistrationEntity,
    onDismiss: () -> Unit,
    onViewPrivateRoom: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = SuccessGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("REGISTRATION CONFIRMED! 🎟️", fontWeight = FontWeight.Black, color = SuccessGreen, fontSize = 15.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Registration ID: #REG-${registration.id}", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("FF UID: ${registration.ffUid}", color = PurplePrimary, fontWeight = FontWeight.Medium)
                if (registration.squadName.isNotBlank()) {
                    Text("Squad: ${registration.squadName}", color = GoldAccent, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("You now have access to the Private Tournament Info page for Room ID & Password!", color = TextSecondary, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onViewPrivateRoom()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("OPEN ROOM INFO", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = Color(0xFF1E1B2E),
        shape = RoundedCornerShape(20.dp)
    )
}

