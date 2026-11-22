package com.shameem.finora.screens.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.shameem.finora.R
import com.shameem.finora.core.designsystem.FinoraColors

data class OnboardingModel(
    val id: Int,
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int,
    val backgroundColor: Color,
    val titleColor: Color,
    val descriptionColor: Color,
    val button: OnboardingButton,
    val shortNote: String,
)

enum class OnboardingButton(val title: String) {
    Next("Next ->"),
    GetStarted("Get Started"),
}

enum class OnboardingStep(val model: OnboardingModel) {
    First(
        OnboardingModel(
            id = 1,
            title = "Take Control",
            description = "Track every income and expense effortlessly. Understand where your money goes and make smarter financial decisions every day.",
            imageRes = R.drawable.onboard_first,
            backgroundColor = FinoraColors.BrandPrimary,
            titleColor = FinoraColors.BrandSecondary,
            descriptionColor = FinoraColors.BrandSecondary,
            button = OnboardingButton.Next,
            shortNote = "Your financial journey\nstarts here",
        ),
    ),
    Second(
        OnboardingModel(
            id = 2,
            title = "Plan Today",
            description = "Manage your budgets, loans, savings and financial goals in one place. Build better money habits and achieve your dreams.",
            imageRes = R.drawable.onboard_second,
            backgroundColor = FinoraColors.BrandPrimary,
            titleColor = FinoraColors.BrandSecondary,
            descriptionColor = FinoraColors.BrandSecondary,
            button = OnboardingButton.Next,
            shortNote = "Budget, save, and plan your\nfinancial future with ease",
        ),
    ),
    Third(
        OnboardingModel(
            id = 3,
            title = "Your Privacy",
            description = "Your financial information stays on your device. No account, no internet connection, and no cloud storage required.",
            imageRes = R.drawable.onboard_third,
            backgroundColor = FinoraColors.BrandPrimary,
            titleColor = FinoraColors.BrandSecondary,
            descriptionColor = FinoraColors.BrandSecondary,
            button = OnboardingButton.GetStarted,
            shortNote = "Your privacy is our priority.\nYour data stays with you",
        ),
    );

    val next: OnboardingStep? get() = entries.getOrNull(ordinal + 1)
    val previous: OnboardingStep? get() = entries.getOrNull(ordinal - 1)
    val isLast: Boolean get() = this == Third
}
