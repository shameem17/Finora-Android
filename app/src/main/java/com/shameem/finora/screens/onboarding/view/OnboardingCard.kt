package com.shameem.finora.screens.onboarding.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.screens.onboarding.OnboardingModel

/** One onboarding page: illustration, headline, copy and the primary action. */
@Composable
fun OnboardingCard(
    data: OnboardingModel,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Image(
                painter = painterResource(data.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(top = 30.dp)
                    .fillMaxWidth()
                    .height(maxHeight * 0.40f),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                AppText(
                    text = data.title,
                    size = 56,
                    weight = AppFontWeight.ExtraBold,
                    color = data.titleColor,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 35.dp, bottom = 16.dp),
                )

                AppText(
                    text = data.description,
                    size = 20,
                    weight = AppFontWeight.Bold,
                    color = data.descriptionColor,
                )

                Spacer(Modifier.weight(1f))
                NextRow(data = data, onNext = onNext)
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NextRow(
    data: OnboardingModel,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Dark capsule sitting on a yellow capsule offset 4dp down/right: the "3D" button.
        val accent = FinoraColors.OnboardingAccent
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    ambientColor = accent.copy(alpha = 0.8f),
                    spotColor = accent.copy(alpha = 0.8f),
                )
                .background(accent, CircleShape)
                .padding(end = 4.dp, bottom = 4.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(data.titleColor)
                    .clickable(role = Role.Button, onClick = onNext)
                    .padding(horizontal = 26.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                AppText(
                    text = data.button.title,
                    size = 20,
                    weight = AppFontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                )
            }
        }

        AppText(
            text = data.shortNote,
            size = 12,
            color = FinoraColors.CustomBlack,
            textAlign = TextAlign.End,
            maxLines = 2,
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(start = 12.dp),
        )
    }
}
