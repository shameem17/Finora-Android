package com.shameem.finora.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.CustomColor
import com.shameem.finora.core.designsystem.FinoraColors

/** Full-width "+ Title" capsule pinned above the tab bar on list screens (iOS `AddActionButton`). */
@Composable
fun AddActionButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: CustomColor = CustomColor.Accent,
    icon: String = "plus",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .shadow(12.dp, CircleShape, ambientColor = tint.color.copy(alpha = 0.4f), spotColor = tint.color.copy(alpha = 0.4f))
            .clip(CircleShape)
            .background(tint.color)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(sfSymbolIcon(icon), contentDescription = null, tint = FinoraColors.OnAccent, modifier = Modifier.size(24.dp))
        AppText(text = title, size = 20, weight = AppFontWeight.ExtraBold, color = FinoraColors.OnAccent, maxLines = 1)
    }
}
