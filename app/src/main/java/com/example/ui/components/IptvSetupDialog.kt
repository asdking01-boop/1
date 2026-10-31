package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.NovaBlack
import com.example.ui.theme.NovaCardBg
import com.example.ui.theme.NovaCardBorder
import com.example.ui.theme.NovaGold
import com.example.ui.theme.NovaRed
import com.example.ui.theme.NovaSurfaceDark
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IptvSetupDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConnectXtream: (name: String, server: String, user: String, pass: String) -> Unit,
    onConnectM3u: (name: String, url: String) -> Unit,
    onLoadDemo: () -> Unit,
    isSyncing: Boolean,
    syncMessage: String?,
    syncError: String?,
    canDismiss: Boolean = true
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Xtream state
    var xtreamName by remember { mutableStateOf("") }
    var xtreamServer by remember { mutableStateOf("") }
    var xtreamUser by remember { mutableStateOf("") }
    var xtreamPass by remember { mutableStateOf("") }

    // M3U state
    var m3uName by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = { if (canDismiss && !isSyncing) onDismiss() },
        sheetState = sheetState,
        containerColor = NovaSurfaceDark,
        contentColor = NovaTextPrimary,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NovaRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.setup_title),
                            color = NovaTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.setup_subtitle),
                            color = NovaTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                if (canDismiss) {
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSyncing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = NovaTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = NovaCardBg,
                contentColor = NovaRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NovaRed
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = stringResource(R.string.tab_xtream),
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) NovaRed else NovaTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = stringResource(R.string.tab_m3u),
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) NovaRed else NovaTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = stringResource(R.string.tab_demo),
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) NovaGold else NovaTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Xtream Codes
                    OutlinedTextField(
                        value = xtreamName,
                        onValueChange = { xtreamName = it },
                        label = { Text(stringResource(R.string.account_name)) },
                        colors = textFieldColors(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("xtream_name_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = xtreamServer,
                        onValueChange = { xtreamServer = it },
                        label = { Text(stringResource(R.string.server_url)) },
                        placeholder = { Text("http://example.com:8080") },
                        colors = textFieldColors(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("xtream_server_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = xtreamUser,
                        onValueChange = { xtreamUser = it },
                        label = { Text(stringResource(R.string.username)) },
                        colors = textFieldColors(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("xtream_user_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = xtreamPass,
                        onValueChange = { xtreamPass = it },
                        label = { Text(stringResource(R.string.password)) },
                        colors = textFieldColors(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("xtream_pass_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onConnectXtream(xtreamName, xtreamServer, xtreamUser, xtreamPass)
                        },
                        enabled = !isSyncing && xtreamServer.isNotBlank() && xtreamUser.isNotBlank() && xtreamPass.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("connect_xtream_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(syncMessage ?: stringResource(R.string.syncing_playlist))
                        } else {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.connect), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                1 -> {
                    // M3U Playlist
                    OutlinedTextField(
                        value = m3uName,
                        onValueChange = { m3uName = it },
                        label = { Text(stringResource(R.string.account_name)) },
                        colors = textFieldColors(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("m3u_name_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = m3uUrl,
                        onValueChange = { m3uUrl = it },
                        label = { Text(stringResource(R.string.m3u_url)) },
                        placeholder = { Text("https://example.com/playlist.m3u") },
                        colors = textFieldColors(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("m3u_url_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onConnectM3u(m3uName, m3uUrl)
                        },
                        enabled = !isSyncing && m3uUrl.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("connect_m3u_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(syncMessage ?: stringResource(R.string.syncing_playlist))
                        } else {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.connect), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                2 -> {
                    // Demo Showcase
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NovaCardBg)
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = NovaGold,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.tab_demo),
                                    color = NovaGold,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.demo_description),
                                color = NovaTextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onLoadDemo,
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = NovaGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("load_demo_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = NovaBlack, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(syncMessage ?: stringResource(R.string.syncing_playlist), color = NovaBlack)
                        } else {
                            Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = NovaBlack, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.try_demo),
                                color = NovaBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Sync Error
            if (syncError != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = syncError,
                    color = NovaRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NovaRed,
    unfocusedBorderColor = NovaCardBorder,
    focusedLabelColor = NovaRed,
    unfocusedLabelColor = NovaTextSecondary,
    focusedTextColor = NovaTextPrimary,
    unfocusedTextColor = NovaTextPrimary,
    focusedContainerColor = NovaCardBg,
    unfocusedContainerColor = NovaCardBg
)
