package com.shameem.finora.screens.onboarding.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.FinoraTheme
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.screens.onboarding.OnboardingStep
import com.shameem.finora.screens.onboarding.OnboardingViewModel
import com.shameem.finora.screens.onboarding.OnboardingViewModelProtocol
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OnboardingScreen(
    model: OnboardingViewModelProtocol,
    modifier: Modifier = Modifier,
) {
    val steps = OnboardingStep.entries
    val step by model.currentStep.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = step.ordinal) { steps.size }

    // ViewModel -> pager (Next button / programmatic changes).
    LaunchedEffect(step) {
        if (pagerState.currentPage != step.ordinal) {
            pagerState.animateScrollToPage(step.ordinal)
        }
    }
    // Pager -> ViewModel (user swipes).
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collectLatest { page ->
            model.goTo(steps[page])
        }
    }

    val background by animateColorAsState(
        targetValue = steps[pagerState.currentPage].model.backgroundColor,
        animationSpec = tween(durationMillis = 500),
        label = "onboarding-background",
    )

    Box(modifier.fillMaxSize().background(background)) {
        // White wave covering the lower half of the screen.
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.9f), WaveShape(amplitude = 20.dp, frequency = 1f, baseline = 0.52f)),
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        ) { page ->
            OnboardingCard(
                data = steps[page].model,
                onNext = model::next,
            )
        }

        SkipButton(
            onClick = model::skip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp)
                .padding(end = 16.dp),
        )
    }
}

@Composable
private fun SkipButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(FinoraColors.SkipButton)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 5.dp),
    ) {
        AppText(
            text = "Skip",
            size = 20,
            weight = AppFontWeight.Bold,
            color = Color.White,
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun OnboardingScreenPreview() {
    FinoraTheme { OnboardingScreen(model = OnboardingViewModel()) }
}
