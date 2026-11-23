package com.shameem.finora.screens.settings.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.heroCard
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.sectionCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.screens.settings.model.PrivacyPolicy

@Composable
fun PrivacyPolicyView(
    modifier: Modifier = Modifier,
    policy: PrivacyPolicy = PrivacyPolicy.current,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(FinoraColors.ScreenBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "hero") { Hero(policy.summary) }
        items(policy.sections, key = { it.title }) { SectionCard(it) }
        item(key = "effective") {
            AppText(
                text = "Effective ${policy.effectiveDate}",
                size = 12,
                weight = AppFontWeight.Medium,
                color = FinoraColors.TextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun Hero(summary: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heroCard()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = sfSymbolIcon("lock.shield.fill"),
            contentDescription = null,
            tint = FinoraColors.Accent,
            modifier = Modifier.size(28.dp),
        )
        AppText("Private by design", size = 22, weight = AppFontWeight.ExtraBold, color = FinoraColors.OnHero)
        AppText(summary, size = 14, weight = AppFontWeight.Medium, color = FinoraColors.OnHeroSecondary)
    }
}

@Composable
private fun SectionCard(section: PrivacyPolicy.Section) {
    Column(
        modifier = Modifier.sectionCard(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(28.dp).background(FinoraColors.Accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = sfSymbolIcon(section.icon),
                    contentDescription = null,
                    tint = FinoraColors.OnAccent,
                    modifier = Modifier.size(15.dp),
                )
            }
            AppText(section.title, size = 16, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary)
        }
        section.paragraphs.forEach {
            AppText(it, size = 14, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary)
        }
        section.bullets.forEach { bullet ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .padding(top = 7.dp)
                        .size(6.dp)
                        .background(FinoraColors.Accent, CircleShape),
                )
                AppText(bullet, size = 14, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary)
            }
        }
    }
}
