package com.shameem.finora.core.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.CustomColor
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.core.util.DecimalInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Presents [content] in a full-height bottom sheet on the cream background; the Compose counterpart of
 * a SwiftUI `.sheet`. Pair it with [FormSheet] for the standard form chrome.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormSheetHost(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = FinoraColors.ScreenBackground,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        content()
    }
}

/**
 * Standard form chrome: inline title, Cancel on the left, scrolling body and a primary button pinned to the
 * bottom (iOS `FormSheet`). [onSubmit] returns true when the form was accepted, which dismisses the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormSheet(
    title: String,
    primaryTitle: String,
    onDismiss: () -> Unit,
    onSubmit: () -> Boolean,
    modifier: Modifier = Modifier,
    tint: CustomColor = CustomColor.Accent,
    isEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        containerColor = FinoraColors.ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    AppText(
                        text = title,
                        size = 17,
                        weight = AppFontWeight.Bold,
                        color = FinoraColors.TextPrimary,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onDismiss) {
                        AppText("Cancel", size = 16, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FinoraColors.ScreenBackground),
            )
        },
        bottomBar = {
            FormPrimaryButton(
                title = primaryTitle,
                tint = tint,
                isEnabled = isEnabled,
                onClick = {
                    focusManager.clearFocus()
                    if (onSubmit()) onDismiss()
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

/**
 * Large currency amount input with sanitising (iOS `AmountField`). Tapping anywhere on the card focuses the
 * field. The text shrinks (down to half size) as it gets longer, like iOS `minimumScaleFactor(0.5)`.
 */
@Composable
fun AmountField(
    title: String,
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: Int = 40,
    accessory: @Composable () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    val scale = (9f / text.length.coerceAtLeast(9)).coerceAtLeast(0.5f)

    Column(
        modifier = modifier
            .sectionCard()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                focusRequester.requestFocus()
            },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionLabel(title)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AppText(
                text = CurrencyFormatter.symbol,
                size = fontSize - 4,
                weight = AppFontWeight.ExtraBold,
                color = FinoraColors.TextTertiary,
            )
            Box(Modifier.weight(1f)) {
                if (text.isEmpty()) {
                    AppText(
                        text = "0",
                        size = fontSize,
                        weight = AppFontWeight.ExtraBold,
                        color = FinoraColors.TextTertiary,
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = { new ->
                        val clean = DecimalInput.sanitize(new)
                        onTextChange(clean)
                    },
                    singleLine = true,
                    textStyle = appTextStyle(size = fontSize, weight = AppFontWeight.ExtraBold).let {
                        it.copy(color = FinoraColors.TextPrimary, fontSize = it.fontSize * scale)
                    },
                    cursorBrush = SolidColor(FinoraColors.TextPrimary),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                )
            }
        }
        accessory()
    }
}

/** Quick-fill capsule shown under an amount (iOS `AmountChip`). */
@Composable
fun AmountChip(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = FinoraColors.Accent,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.18f))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        AppText(
            text = title,
            size = 12,
            weight = AppFontWeight.Bold,
            color = FinoraColors.TextPrimary,
            maxLines = 1,
        )
    }
}

/** Something that owns an editable logo (iOS `LogoEditing`). */
interface LogoEditing {
    var logoData: ByteArray?
    var isProcessingLogo: Boolean
}

/** Processes [data] into a small PNG and stores it on the editor (iOS `LogoEditing.setLogo`). */
suspend fun LogoEditing.setLogo(fromImageData: ByteArray) {
    isProcessingLogo = true
    try {
        LogoImageProcessor.makeLogo(fromImageData)?.let { logoData = it }
    } finally {
        isProcessingLogo = false
    }
}

/** Logo chooser + colour swatches in one card (iOS `LogoColorPickerCard`). */
@Composable
fun LogoColorPickerCard(
    tintHex: String,
    onTintChange: (String) -> Unit,
    hasLogo: Boolean,
    isProcessing: Boolean,
    onPickImage: suspend (ByteArray) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bytes = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            if (bytes != null) onPickImage(bytes)
        }
    }

    Column(
        modifier = modifier.sectionCard(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionLabel("Logo")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionPill(
                title = if (hasLogo) "Change logo" else "Choose logo",
                icon = "photo",
                foreground = FinoraColors.TextPrimary,
                background = FinoraColors.FillSubtle,
                onClick = {
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            )
            if (hasLogo) {
                ActionPill(
                    title = "Remove",
                    icon = null,
                    foreground = FinoraColors.Negative,
                    background = FinoraColors.Negative.copy(alpha = 0.1f),
                    onClick = onRemove,
                )
            }
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = FinoraColors.TextPrimary,
                )
            }
        }
        HorizontalDivider(color = FinoraColors.Hairline)
        SectionLabel("Colour")
        ColorSwatchPicker(hex = tintHex, onHexChange = onTintChange)
    }
}

@Composable
private fun ActionPill(
    title: String,
    icon: String?,
    foreground: Color,
    background: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) {
            Icon(sfSymbolIcon(icon), contentDescription = null, tint = foreground, modifier = Modifier.size(16.dp))
        }
        AppText(text = title, size = 14, weight = AppFontWeight.Bold, color = foreground, maxLines = 1)
    }
}
