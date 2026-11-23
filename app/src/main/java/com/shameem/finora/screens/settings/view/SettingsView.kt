package com.shameem.finora.screens.settings.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.shameem.finora.app.AppSettings
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.FinoraSwitch
import com.shameem.finora.core.ui.FormSheetHost
import com.shameem.finora.core.ui.ScreenTitle
import com.shameem.finora.core.ui.SectionLabel
import com.shameem.finora.core.ui.sectionCard
import com.shameem.finora.core.ui.sfSymbolIcon

@Composable
fun SettingsView(
    settings: AppSettings,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showsPrivacyPolicy by remember { mutableStateOf(false) }
    val version = remember { appVersion(context) }

    // Android 13+ needs a runtime grant before loan reminders can be shown.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        settings.updateNotificationsEnabled(granted)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinoraColors.ScreenBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitle("Settings")
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Group("Preferences") {
                SettingsRow(
                    icon = "bell",
                    title = "Loan reminders",
                    subtitle = "Local notifications for due dates",
                ) {
                    FinoraSwitch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { enabled ->
                            val needsPermission = enabled &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                                PackageManager.PERMISSION_GRANTED
                            if (needsPermission) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                settings.updateNotificationsEnabled(enabled)
                            }
                        },
                    )
                }
                RowDivider()
                InfoRow(
                    icon = "banknote",
                    title = "Currency",
                    value = "${settings.currency.symbol} ${settings.currency.name.uppercase()}",
                )
            }
            Group("Privacy") {
                SettingsRow(
                    icon = "lock.shield",
                    title = "Privacy Policy",
                    subtitle = "How Finora handles your data",
                    modifier = Modifier.clickable(role = Role.Button) { showsPrivacyPolicy = true },
                ) {
                    Icon(
                        imageVector = sfSymbolIcon("chevron.right"),
                        contentDescription = null,
                        tint = FinoraColors.TextTertiary,
                        modifier = Modifier.size(16.dp),
                    )
                }
                RowDivider()
                InfoRow(icon = "iphone", title = "Storage", value = "On this device only")
            }
            Group("About") {
                InfoRow(icon = "info.circle", title = "Version", value = version)
            }
        }
    }

    if (showsPrivacyPolicy) {
        FormSheetHost(onDismissRequest = { showsPrivacyPolicy = false }) {
            Column(Modifier.fillMaxSize().background(FinoraColors.ScreenBackground)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppText(
                        "Privacy Policy",
                        size = 17,
                        weight = AppFontWeight.Bold,
                        color = FinoraColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { showsPrivacyPolicy = false }) {
                        AppText("Done", size = 16, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary)
                    }
                }
                PrivacyPolicyView()
            }
        }
    }
}

private fun appVersion(context: android.content.Context): String = runCatching {
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong()
    "${info.versionName ?: "1.0"} ($code)"
}.getOrDefault("1.0 (1)")

@Composable
private fun Group(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(title, Modifier.padding(horizontal = 4.dp))
        Column(modifier = Modifier.sectionCard(padding = 6.dp), content = content)
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 52.dp), color = FinoraColors.Hairline)
}

@Composable
private fun SettingsRow(
    icon: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(32.dp).background(FinoraColors.Accent, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(sfSymbolIcon(icon), contentDescription = null, tint = FinoraColors.OnAccent, modifier = Modifier.size(17.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppText(title, size = 15, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1)
            if (subtitle != null) {
                AppText(subtitle, size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary, maxLines = 1)
            }
        }
        trailing()
    }
}

@Composable
private fun InfoRow(icon: String, title: String, value: String) {
    SettingsRow(icon = icon, title = title) {
        AppText(value, size = 14, weight = AppFontWeight.SemiBold, color = FinoraColors.TextSecondary, maxLines = 1)
    }
}
