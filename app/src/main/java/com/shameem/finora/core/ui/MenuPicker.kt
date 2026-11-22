package com.shameem.finora.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors

/**
 * Compact dropdown that shows the current choice with an up/down chevron (iOS `Picker(...).pickerStyle(.menu)`).
 * [icon] optionally returns an SF Symbol name for each option, shown in the menu.
 */
@Composable
fun <T> MenuPicker(
    options: List<T>,
    selection: T,
    onSelect: (T) -> Unit,
    title: (T) -> String,
    modifier: Modifier = Modifier,
    icon: ((T) -> String)? = null,
    isEnabled: (T) -> Boolean = { true },
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        Row(
            modifier = Modifier
                .clickable(role = Role.DropdownList) { expanded = true }
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            AppText(
                text = title(selection),
                size = 15,
                weight = AppFontWeight.SemiBold,
                color = FinoraColors.TextPrimary,
                maxLines = 1,
            )
            Icon(
                imageVector = sfSymbolIcon("chevron.up.chevron.down"),
                contentDescription = null,
                tint = FinoraColors.TextPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        AppText(
                            text = title(option),
                            size = 15,
                            weight = if (option == selection) AppFontWeight.Bold else AppFontWeight.SemiBold,
                            color = FinoraColors.TextPrimary,
                        )
                    },
                    leadingIcon = icon?.let { symbol ->
                        { Icon(sfSymbolIcon(symbol(option)), contentDescription = null, modifier = Modifier.size(20.dp)) }
                    },
                    enabled = isEnabled(option),
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
