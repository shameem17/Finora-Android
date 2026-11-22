package com.shameem.finora.screens.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface OnboardingViewModelProtocol {
    val currentStep: StateFlow<OnboardingStep>

    fun next()
    fun previous()

    /** Called when the user swipes the pager to [step]. */
    fun goTo(step: OnboardingStep)

    fun skip()
}

class OnboardingViewModel(
    private val onFinished: () -> Unit = {},
) : ViewModel(), OnboardingViewModelProtocol {

    private val _currentStep = MutableStateFlow(OnboardingStep.First)
    override val currentStep: StateFlow<OnboardingStep> = _currentStep.asStateFlow()

    private var isFinished = false

    override fun next() {
        val nextStep = _currentStep.value.next
        if (nextStep != null) _currentStep.value = nextStep else finish()
    }

    override fun previous() {
        _currentStep.value.previous?.let { _currentStep.value = it }
    }

    override fun goTo(step: OnboardingStep) {
        _currentStep.value = step
    }

    override fun skip() = finish()

    /** Guards against double-taps firing the completion callback twice. */
    private fun finish() {
        if (isFinished) return
        isFinished = true
        onFinished()
    }
}
