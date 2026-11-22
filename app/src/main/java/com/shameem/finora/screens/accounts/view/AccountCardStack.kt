package com.shameem.finora.screens.accounts.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ShrinkToFitText
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.home.view.AccountCard
import com.shameem.finora.screens.home.view.CardBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private val CardHeight = 200.dp
private val LiftHeight = 70.dp
private val IdealPeek = 38.dp
private val MinPeek = 26.dp
private val MaxExtraHeight = 230.dp
private val StackSpring = spring<Float>(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)

/**
 * Wallet-style stack of account cards (iOS `AccountCardStack`). Tap the front card to flip it, swipe it away
 * to bring the next card forward, tap a card behind to lift it to the front.
 */
@Composable
fun AccountCardStack(
    accounts: List<Account>,
    frontId: UUID?,
    onFrontChange: (UUID?) -> Unit,
    onEdit: (Account) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val flippedFront: SnapshotStateList<UUID> = remember { mutableStateListOf() }
    val dragOffset = remember { Animatable(0f) } // px
    var rising by remember { mutableStateOf<UUID?>(null) }

    // Keep a valid front card as the list changes (iOS `ensureFront`).
    LaunchedEffect(accounts.map { it.id }) {
        if (frontId == null || accounts.none { it.id == frontId }) onFrontChange(accounts.firstOrNull()?.id)
        flippedFront.removeAll { id -> accounts.none { it.id == id } }
    }

    val maxVisible = 1 + (MaxExtraHeight / MinPeek).toInt()
    val ordered = remember(accounts, frontId) {
        val index = accounts.indexOfFirst { it.id == frontId }
        if (index < 0) accounts else accounts.drop(index) + accounts.take(index)
    }
    val visible = ordered.take(maxVisible)
    val visibleCount = min(accounts.size, maxVisible)
    val behind = max(accounts.size - 1, 1)
    val peek: Dp = min(IdealPeek.value, max(MaxExtraHeight.value / behind, MinPeek.value)).dp
    val scaleStep = min(0.045f, 0.16f / max(visibleCount - 1, 1))

    fun advance() {
        if (ordered.size > 1) {
            flippedFront.clear()
            onFrontChange(ordered[1].id)
        }
    }

    fun tap(account: Account, isFront: Boolean) {
        if (rising != null) return
        if (isFront) {
            if (!flippedFront.remove(account.id)) flippedFront.add(account.id)
            return
        }
        scope.launch {
            flippedFront.clear()
            rising = account.id
            delay(200)
            onFrontChange(account.id)
            rising = null
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CardHeight + peek * max(visible.size - 1, 0)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            // Draw back-to-front so the front card ends up on top.
            visible.forEachIndexed { position, account ->
                key(account.id) {
                    StackCard(
                        account = account,
                        position = position,
                        peek = peek,
                        scaleStep = scaleStep,
                        isFlipped = account.id in flippedFront,
                        lift = if (rising == account.id) LiftHeight else 0.dp,
                        dragOffsetPx = if (position == 0) dragOffset.value else 0f,
                        density = density.density,
                        onTap = { tap(account, position == 0) },
                        onDrag = { delta -> scope.launch { dragOffset.snapTo(dragOffset.value + delta) } },
                        onDragEnd = {
                            val shouldAdvance = abs(dragOffset.value / density.density) > 70 && accounts.size > 1
                            scope.launch { dragOffset.animateTo(0f, StackSpring) }
                            if (shouldAdvance) advance()
                        },
                    )
                }
            }
        }
        StackFooter(
            accounts = accounts,
            frontId = frontId,
            visibleCount = visibleCount,
            front = ordered.firstOrNull(),
            onEdit = onEdit,
        )
    }
}

@Composable
private fun StackCard(
    account: Account,
    position: Int,
    peek: Dp,
    scaleStep: Float,
    isFlipped: Boolean,
    lift: Dp,
    dragOffsetPx: Float,
    density: Float,
    onTap: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    val isFront = position == 0
    val yOffset by animateDpAsState(
        targetValue = -(peek * position + lift),
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "card-y",
    )
    val scale by animateFloatAsState(1f - position * scaleStep, StackSpring, label = "card-scale")
    val angle by animateFloatAsState(if (isFront && isFlipped) 180f else 0f, StackSpring, label = "card-flip")
    val shadowElevation by animateDpAsState(if (isFront) 14.dp else 5.dp, label = "card-shadow")
    val dragDp = dragOffsetPx / density

    Box(
        modifier = Modifier
            .zIndex((100 - position).toFloat())
            .graphicsLayer {
                translationY = yOffset.toPx()
                translationX = dragOffsetPx
                scaleX = scale
                scaleY = scale
                rotationZ = if (isFront) dragDp / 40f else 0f
                transformOrigin = TransformOrigin(0.5f, 1f)
            }
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(26.dp),
                ambientColor = FinoraColors.BrandSecondary.copy(alpha = if (isFront) 0.25f else 0.18f),
                spotColor = FinoraColors.BrandSecondary.copy(alpha = if (isFront) 0.25f else 0.18f),
            )
            .pointerInput(account.id) { detectTapGestures(onTap = { onTap() }) }
            .pointerInput(isFront) {
                if (isFront) {
                    detectHorizontalDragGestures(
                        onDragEnd = onDragEnd,
                        onDragCancel = onDragEnd,
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            onDrag(amount)
                        },
                    )
                }
            }
            .semantics(mergeDescendants = true) {
                contentDescription = "${account.name}, ${account.type.title}"
                role = Role.Button
            },
    ) {
        FlipCard(
            angle = angle,
            front = {
                Box {
                    AccountCard(account = account, showsContent = isFront, modifier = Modifier.height(CardHeight))
                    if (!isFront) PeekHeader(account = account, peek = peek, modifier = Modifier.align(Alignment.TopStart))
                }
            },
            back = { AccountCardBack(account = account, modifier = Modifier.height(CardHeight)) },
        )
    }
}

/** Two-sided card that rotates around its vertical axis (iOS `FlipCard`). */
@Composable
private fun FlipCard(
    angle: Float,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
) {
    val showsFront = angle % 360f < 90f
    Box(
        Modifier.graphicsLayer {
            rotationY = angle
            cameraDistance = 12f * density
        },
    ) {
        if (showsFront) {
            front()
        } else {
            // Pre-rotate the back so it reads correctly once the card has turned over.
            Box(Modifier.graphicsLayer { rotationY = 180f }) { back() }
        }
    }
}

@Composable
private fun PeekHeader(account: Account, peek: Dp, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(peek)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(sfSymbolIcon(account.type.symbol), contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        ShrinkToFitText(
            text = account.name,
            style = appTextStyle(size = if (peek < 32.dp) 12 else 14, weight = AppFontWeight.Bold),
            color = Color.White,
            minScale = 0.7f,
            modifier = Modifier.weight(1f),
        )
        AppText(
            text = CurrencyFormatter.taka(account.balance),
            size = 14,
            weight = AppFontWeight.Bold,
            color = Color.White,
            maxLines = 1,
        )
    }
}

@Composable
private fun StackFooter(
    accounts: List<Account>,
    frontId: UUID?,
    visibleCount: Int,
    front: Account?,
    onEdit: (Account) -> Unit,
) {
    val index = accounts.indexOfFirst { it.id == frontId }
    val counter = if (index < 0) {
        ""
    } else {
        val hidden = accounts.size - visibleCount
        val more = if (hidden > 0) " · +$hidden more behind" else ""
        "${index + 1} of ${accounts.size}$more · tap to flip, swipe to switch"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShrinkToFitText(
            text = counter,
            style = appTextStyle(size = 13, weight = AppFontWeight.SemiBold),
            color = FinoraColors.TextSecondary,
            minScale = 0.7f,
            modifier = Modifier.weight(1f),
        )
        if (front != null) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(FinoraColors.Accent)
                    .clickable(role = Role.Button) { onEdit(front) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(sfSymbolIcon("pencil"), contentDescription = null, tint = FinoraColors.OnAccent, modifier = Modifier.size(14.dp))
                AppText("Edit", size = 13, weight = AppFontWeight.Bold, color = FinoraColors.OnAccent)
            }
        }
    }
}

/** Reverse side of an account card: account number, type, opening balance and net activity. */
@Composable
fun AccountCardBack(account: Account, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.2f), shape),
    ) {
        CardBackground(tint = account.tint, modifier = Modifier.matchParentSize())
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .padding(top = 22.dp)
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(Color.Black.copy(alpha = 0.35f)),
            )
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                BackDetail(
                    title = "Account number",
                    value = spaced(account.number),
                    valueStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BackDetail("Type", account.type.title, Modifier.weight(1f))
                    BackDetail("Opening", CurrencyFormatter.taka(account.initialBalance), Modifier.weight(1f))
                    BackDetail("Activity", movement(account.balance - account.initialBalance), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BackDetail(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = appTextStyle(size = 13, weight = AppFontWeight.Bold),
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        AppText(
            text = title.uppercase(),
            size = 11,
            weight = AppFontWeight.Medium,
            color = Color.White.copy(alpha = 0.65f),
            letterSpacing = 0.8.sp,
        )
        ShrinkToFitText(
            text = value,
            style = valueStyle,
            color = Color.White,
            minScale = 0.7f,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun movement(value: Double): String {
    val sign = if (value > 0) "+" else if (value < 0) "−" else ""
    return sign + CurrencyFormatter.taka(abs(value))
}

/** `1234567894821` -> `1234 5678 9482 1`; no digits -> "No account number". */
private fun spaced(number: String): String {
    val digits = number.filterNot(Char::isWhitespace)
    if (digits.isEmpty()) return "No account number"
    return digits.chunked(4).joinToString(" ")
}
