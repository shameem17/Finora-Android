package com.shameem.finora.screens.settings.model

/** The in-app privacy policy (iOS `PrivacyPolicy`). Wording is adapted to Android where the platform differs. */
data class PrivacyPolicy(
    val effectiveDate: String,
    val summary: String,
    val sections: List<Section>,
) {
    data class Section(
        /** SF Symbol name, resolved by `sfSymbolIcon`. */
        val icon: String,
        val title: String,
        val paragraphs: List<String> = emptyList(),
        val bullets: List<String> = emptyList(),
    )

    companion object {
        val current = PrivacyPolicy(
            effectiveDate = "November 19, 2026",
            summary = "Finora keeps everything on your device. There is no account, no server and no tracking. " +
                "We cannot see your data, because it never leaves your phone.",
            sections = listOf(
                Section(
                    icon = "iphone",
                    title = "Your data stays on your device",
                    paragraphs = listOf(
                        "Finora works fully offline. The information you enter is stored only in the app's private storage on your phone and is never sent to us or to anyone else.",
                        "Finora does not require you to sign up or log in, and it has no backend server.",
                    ),
                ),
                Section(
                    icon = "list.bullet.rectangle",
                    title = "Information you enter",
                    paragraphs = listOf("To track your finances, you may add the following to the app:"),
                    bullets = listOf(
                        "Accounts: names, account or card numbers you choose to type, account type, colour, logo and balances (bank, mobile wallet, card, cash and similar).",
                        "Income, expenses and transfers between your accounts, including amounts, dates, categories and notes.",
                        "Loans you have lent or borrowed, repayments, due dates, and the names (and any notes) of the people involved.",
                        "Savings goals, deposits, withdrawals and targets.",
                        "Categories and monthly budgets you create.",
                    ),
                ),
                Section(
                    icon = "person.2",
                    title = "Information about other people",
                    paragraphs = listOf(
                        "If you record a loan, you may type another person's name or a note about them. That information is stored only on your device, together with the rest of your data, and is used only to show you your own records. Please enter only what you are comfortable keeping.",
                    ),
                ),
                Section(
                    icon = "photo",
                    title = "Photos",
                    paragraphs = listOf(
                        "You can pick an image as an account logo. Finora uses the system photo picker, so the app can only see the single image you choose, not your photo library. The chosen image is stored in the app as a small copy and is never uploaded.",
                    ),
                ),
                Section(
                    icon = "bell",
                    title = "Notifications",
                    paragraphs = listOf(
                        "If you allow notifications, Finora schedules local reminders for loan due dates. These are created and delivered by Android on your device; no push-notification service or server is involved. You can turn reminders off in Settings or in Android's system settings at any time.",
                    ),
                ),
                Section(
                    icon = "hand.raised",
                    title = "What we do not do",
                    bullets = listOf(
                        "We do not collect, transmit, sell or share your personal or financial data.",
                        "We do not use analytics, advertising, tracking or crash-reporting services.",
                        "We do not include third-party SDKs that collect data.",
                        "We do not connect to your bank or read your messages, contacts or location.",
                    ),
                ),
                Section(
                    icon = "lock.shield",
                    title = "Security",
                    paragraphs = listOf(
                        "Your data lives inside the app's private storage, which other apps cannot read, and is protected by the same device encryption that protects the rest of your phone. Set a strong screen lock and keep your device updated to protect it.",
                        "Hide-balances mode masks amounts on screen, but it is a privacy convenience and not a lock.",
                    ),
                ),
                Section(
                    icon = "icloud",
                    title = "Backups",
                    paragraphs = listOf(
                        "Finora does not sync to the cloud and has no cloud features of its own. Android's automatic app backup is turned off for Finora, so its data is not included in Google backups or device-to-device transfers.",
                    ),
                ),
                Section(
                    icon = "trash",
                    title = "Your control and deleting data",
                    paragraphs = listOf(
                        "You can edit or delete any account, entry, loan, person, savings goal or category inside the app. Uninstalling the app removes all of its data from your device. Because we hold no copy, we cannot recover data after it is deleted.",
                    ),
                ),
                Section(
                    icon = "figure.and.child.holdinghands",
                    title = "Children",
                    paragraphs = listOf(
                        "Finora is a general personal-finance tool and is not directed at children under 13. We do not knowingly collect information from anyone, including children.",
                    ),
                ),
                Section(
                    icon = "exclamationmark.circle",
                    title = "Not financial advice",
                    paragraphs = listOf(
                        "Finora helps you record and view your own figures. Totals, insights and summaries are calculated from what you enter and are not financial, tax or legal advice. Keep your own records for anything official.",
                    ),
                ),
                Section(
                    icon = "arrow.triangle.2.circlepath",
                    title = "Changes to this policy",
                    paragraphs = listOf(
                        "If the app's features change in a way that affects privacy, for example by adding optional sync, we will update this policy and the effective date above before the change ships.",
                    ),
                ),
                Section(
                    icon = "envelope",
                    title = "Contact",
                    paragraphs = listOf("For questions about this policy, use the support contact on Finora's Google Play page."),
                ),
            ),
        )
    }
}
