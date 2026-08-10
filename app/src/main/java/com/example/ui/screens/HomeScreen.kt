package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: UserEntity?,
    tournaments: List<TournamentEntity>,
    registrations: List<RegistrationEntity>,
    pinnedBanners: List<PinnedBannerEntity>,
    notices: List<NoticeEntity>,
    filterMode: String,
    onFilterChange: (String) -> Unit,
    onJoinClick: (TournamentEntity) -> Unit,
    onOpenPrivateRoomInfo: (TournamentEntity) -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    onOpenLiveChat: () -> Unit
) {
    val joinedTournamentIds = remember(registrations) {
        registrations.map { it.tournamentId }.toSet()
    }

    val filteredTournaments = remember(tournaments, filterMode) {
        if (filterMode == "ALL") tournaments
        else tournaments.filter { it.gameMode.equals(filterMode, ignoreCase = true) }
    }

    FrostedGlassBackground {
        Scaffold(
            topBar = {
                Surface(
                    color = Color(0x14FFFFFF), // Translucent glass topbar
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                ) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFA855F7), Color(0xFF6366F1))
                                            )
                                        )
                                        .border(1.dp, Color(0x40FFFFFF), CircleShape)
                                        .clickable { onNavigateToProfile() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser?.name?.take(2)?.uppercase() ?: "AX",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Welcome back",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = currentUser?.name ?: "Gamer",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        },
                        actions = {
                            // Glass Wallet Balance Pill Badge
                            Surface(
                                modifier = Modifier
                                    .clickable { onNavigateToWallet() }
                                    .padding(end = 6.dp),
                                color = Color(0x1EFFFFFF),
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2BFFFFFF))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(SuccessGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "৳ ${String.format("%.1f", currentUser?.walletBalance ?: 0.0)}",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(PurplePrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Admin Mode Quick Access
                            IconButton(onClick = onNavigateToAdminPanel) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Mode", tint = GoldAccent)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            floatingActionButton = {
                Surface(
                    onClick = onOpenLiveChat,
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PurplePrimary, IndigoAccent)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Chat, contentDescription = "Live Support", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Live Support", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Pinned Notice Card (Frosted Glass)
                if (notices.isNotEmpty()) {
                    item {
                        val notice = notices.first()
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0x12FFFFFF)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2BFFFFFF)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x20FBBF24)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🔥", fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notice.title,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = notice.content,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Hero Active Tournament Banner (Frosted Glass Overlay)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0x18FFFFFF)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2BFFFFFF))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.banner_esports_1786082485993),
                                contentDescription = "Hero Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color(0xEB0B0A0F))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        color = PurplePrimary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "LIVE INFO",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Surface(
                                        color = Color(0x26FFFFFF),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x26FFFFFF))
                                    ) {
                                        Text(
                                            text = "DUO / SQUAD",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = pinnedBanners.firstOrNull()?.title ?: "ELITE FREE FIRE CHAMPIONSHIP",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Recharge via bKash (01789495251) & Win Huge Cash Prizes!",
                                    color = PurplePrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Categories Filter Chips
                item {
                    Column {
                        Text(
                            text = "TOURNAMENT MODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = TextSecondary,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("ALL", "SOLO", "DUO", "SQUAD").forEach { mode ->
                                val isSelected = filterMode == mode
                                Surface(
                                    onClick = { onFilterChange(mode) },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) PurplePrimary else Color(0x14FFFFFF),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) PurplePrimary else Color(0x20FFFFFF)
                                    )
                                ) {
                                    Text(
                                        text = mode,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Tournament Items List
                items(filteredTournaments) { tournament ->
                    val isJoined = joinedTournamentIds.contains(tournament.id)
                    TournamentCard(
                        tournament = tournament,
                        isJoined = isJoined,
                        onJoinClick = { onJoinClick(tournament) },
                        onOpenPrivateRoomInfo = { onOpenPrivateRoomInfo(tournament) }
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun TournamentCard(
    tournament: TournamentEntity,
    isJoined: Boolean,
    onJoinClick: () -> Unit,
    onOpenPrivateRoomInfo: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0x14FFFFFF)),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isJoined) SuccessGreen else Color(0x20FFFFFF)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Mode tag & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (tournament.gameMode) {
                        "SQUAD" -> GoldAccent
                        "DUO" -> PurplePrimary
                        else -> BkashPink
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = tournament.gameMode,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }

                if (isJoined) {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("REGISTERED", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = "⏰ ${tournament.matchTime}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = tournament.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Frosted Stats Grid (3 Columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Entry
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0x12FFFFFF),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("ENTRY", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text("৳ ${tournament.entryFee.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = PurplePrimary)
                    }
                }

                // Prize
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0x12FFFFFF),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("PRIZE", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text("৳ ${tournament.prizePool.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = SuccessGreen)
                    }
                }

                // Slots
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color(0x12FFFFFF),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("SLOTS", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text("${tournament.filledSlots}/${tournament.totalSlots}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Slots Progress Bar
            LinearProgressIndicator(
                progress = { (tournament.filledSlots.toFloat() / tournament.totalSlots.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PurplePrimary,
                trackColor = Color(0x1FFFFFFF),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            if (isJoined) {
                Button(
                    onClick = onOpenPrivateRoomInfo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("OPEN ROOM ID & CHAT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = onJoinClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("JOIN NOW (৳ ${tournament.entryFee.toInt()} BDT)", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}

