package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.NovaBlack
import com.example.ui.theme.NovaCardBg
import com.example.ui.theme.NovaRed
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import com.example.ui.viewmodel.IptvViewModel

@Composable
fun SettingsScreen(
    viewModel: IptvViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.appLanguage.collectAsState()
    val defaultAspect by viewModel.defaultAspectRatio.collectAsState()
    val autoPlayNext by viewModel.autoPlayNextEpisode.collectAsState()
    val activeAccount by viewModel.activeAccount.collectAsState()

    var showLanguageMenu by remember { mutableStateOf(false) }
    var showAspectMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NovaBlack)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                color = NovaTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }

        // Section: Appearance & Language
        item {
            SectionHeader(title = stringResource(R.string.section_appearance))
            Spacer(modifier = Modifier.height(6.dp))

            // Language Selector Item
            Box {
                SettingsClickableItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.language),
                    value = if (currentLang == "ar") "العربية (افتراضي)" else "English",
                    onClick = { showLanguageMenu = true }
                )

                DropdownMenu(
                    expanded = showLanguageMenu,
                    onDismissRequest = { showLanguageMenu = false },
                    modifier = Modifier.background(NovaCardBg)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.language_arabic),
                                color = if (currentLang == "ar") NovaRed else NovaTextPrimary,
                                fontWeight = if (currentLang == "ar") FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            viewModel.setLanguage("ar")
                            showLanguageMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.language_english),
                                color = if (currentLang == "en") NovaRed else NovaTextPrimary,
                                fontWeight = if (currentLang == "en") FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            viewModel.setLanguage("en")
                            showLanguageMenu = false
                        }
                    )
                }
            }
        }

        // Section: Playback Preferences
        item {
            SectionHeader(title = stringResource(R.string.section_playback))
            Spacer(modifier = Modifier.height(6.dp))

            // Aspect ratio item
            Box {
                SettingsClickableItem(
                    icon = Icons.Default.AspectRatio,
                    title = stringResource(R.string.default_aspect),
                    value = defaultAspect,
                    onClick = { showAspectMenu = true }
                )

                DropdownMenu(
                    expanded = showAspectMenu,
                    onDismissRequest = { showAspectMenu = false },
                    modifier = Modifier.background(NovaCardBg)
                ) {
                    listOf("FIT", "FILL", "ZOOM", "STRETCH").forEach { ratio ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = ratio,
                                    color = if (defaultAspect == ratio) NovaRed else NovaTextPrimary,
                                    fontWeight = if (defaultAspect == ratio) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                viewModel.setDefaultAspectRatio(ratio)
                                showAspectMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Auto-play Next Episode Switch
            SettingsSwitchItem(
                icon = Icons.Default.PlayCircle,
                title = stringResource(R.string.auto_play_next),
                checked = autoPlayNext,
                onCheckedChange = { viewModel.setAutoPlayNextEpisode(it) }
            )
        }

        // Section: IPTV Accounts
        item {
            SectionHeader(title = stringResource(R.string.section_iptv))
            Spacer(modifier = Modifier.height(6.dp))

            SettingsClickableItem(
                icon = Icons.Default.LiveTv,
                title = stringResource(R.string.manage_accounts),
                value = activeAccount?.name ?: "No account",
                onClick = { viewModel.openAccountManager() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsClickableItem(
                icon = Icons.Default.Refresh,
                title = stringResource(R.string.refresh_playlist),
                value = "",
                onClick = {
                    viewModel.loadDemoShowcase()
                    Toast.makeText(context, context.getString(R.string.sync_success), Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsClickableItem(
                icon = Icons.Default.CleaningServices,
                title = stringResource(R.string.clear_cache),
                value = "",
                onClick = {
                    Toast.makeText(context, context.getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Section: About
        item {
            SectionHeader(title = stringResource(R.string.section_about))
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NovaCardBg)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NovaRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "NOVA IPTV",
                        color = NovaTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.app_version),
                    color = NovaRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.app_info_text),
                    color = NovaTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = NovaTextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NovaCardBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = NovaRed, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = NovaTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    color = NovaTextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = NovaTextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NovaCardBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = NovaRed, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = NovaTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NovaRed,
                uncheckedThumbColor = NovaTextSecondary,
                uncheckedTrackColor = NovaBlack
            )
        )
    }
}
