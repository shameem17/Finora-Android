package com.shameem.finora.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

private class SymbolIcons(val outlined: ImageVector, val filled: ImageVector)

/**
 * Maps an iOS SF Symbol name to a Material icon. iOS persists category icons by SF Symbol name, so the
 * same strings are kept in storage and rendered here. A name ending in `.fill` resolves to the filled
 * icon, anything else to the outlined one. Unknown names fall back to a generic icon.
 */
fun sfSymbolIcon(name: String): ImageVector {
    val isFilled = name.endsWith(".fill")
    val base = name.removeSuffix(".fill")
    val icons = table[base] ?: table[name] ?: return if (isFilled) Icons.Filled.Circle else Icons.Outlined.Circle
    return if (isFilled) icons.filled else icons.outlined
}

private val table: Map<String, SymbolIcons> by lazy {
    mapOf(
        "airplane" to SymbolIcons(Icons.Outlined.Flight, Icons.Filled.Flight),
        "arrow.down.circle" to SymbolIcons(Icons.Outlined.ArrowCircleDown, Icons.Filled.ArrowCircleDown),
        "arrow.down.left" to SymbolIcons(Icons.Outlined.SouthWest, Icons.Filled.SouthWest),
        "arrow.down.left.circle" to SymbolIcons(Icons.Outlined.ArrowCircleDown, Icons.Filled.ArrowCircleDown),
        "arrow.down.right" to SymbolIcons(Icons.Outlined.SouthEast, Icons.Filled.SouthEast),
        "arrow.down.to.line" to SymbolIcons(Icons.Outlined.Download, Icons.Filled.Download),
        "arrow.left.arrow.right" to SymbolIcons(Icons.Outlined.SwapHoriz, Icons.Filled.SwapHoriz),
        "arrow.left.arrow.right.circle" to SymbolIcons(Icons.Outlined.SwapHorizontalCircle, Icons.Filled.SwapHorizontalCircle),
        "arrow.triangle.2.circlepath" to SymbolIcons(Icons.Outlined.Sync, Icons.Filled.Sync),
        "arrow.up.arrow.down" to SymbolIcons(Icons.Outlined.SwapVert, Icons.Filled.SwapVert),
        "arrow.up.circle" to SymbolIcons(Icons.Outlined.ArrowCircleUp, Icons.Filled.ArrowCircleUp),
        "arrow.up.right" to SymbolIcons(Icons.Outlined.NorthEast, Icons.Filled.NorthEast),
        "arrow.up.right.circle" to SymbolIcons(Icons.Outlined.ArrowCircleUp, Icons.Filled.ArrowCircleUp),
        "arrow.uturn.backward.circle" to SymbolIcons(Icons.Outlined.Undo, Icons.Filled.Undo),
        "banknote" to SymbolIcons(Icons.Outlined.Payments, Icons.Filled.Payments),
        "bell" to SymbolIcons(Icons.Outlined.Notifications, Icons.Filled.Notifications),
        "bolt" to SymbolIcons(Icons.Outlined.Bolt, Icons.Filled.Bolt),
        "briefcase" to SymbolIcons(Icons.Outlined.Work, Icons.Filled.Work),
        "building.columns" to SymbolIcons(Icons.Outlined.AccountBalance, Icons.Filled.AccountBalance),
        "bus" to SymbolIcons(Icons.Outlined.DirectionsBus, Icons.Filled.DirectionsBus),
        "calendar" to SymbolIcons(Icons.Outlined.CalendarToday, Icons.Filled.CalendarToday),
        "calendar.badge.checkmark" to SymbolIcons(Icons.Outlined.EventAvailable, Icons.Filled.EventAvailable),
        "calendar.badge.clock" to SymbolIcons(Icons.Outlined.Event, Icons.Filled.Event),
        "car" to SymbolIcons(Icons.Outlined.DirectionsCar, Icons.Filled.DirectionsCar),
        "cart" to SymbolIcons(Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart),
        "chart.line.uptrend.xyaxis" to SymbolIcons(Icons.Outlined.ShowChart, Icons.Filled.ShowChart),
        "chevron.right" to SymbolIcons(Icons.Outlined.ChevronRight, Icons.Filled.ChevronRight),
        "checkmark" to SymbolIcons(Icons.Outlined.Check, Icons.Filled.Check),
        "checkmark.circle" to SymbolIcons(Icons.Outlined.CheckCircle, Icons.Filled.CheckCircle),
        "checkmark.seal" to SymbolIcons(Icons.Outlined.Verified, Icons.Filled.Verified),
        "chevron.up.chevron.down" to SymbolIcons(Icons.Outlined.UnfoldMore, Icons.Filled.UnfoldMore),
        "clock.arrow.circlepath" to SymbolIcons(Icons.Outlined.History, Icons.Filled.History),
        "creditcard" to SymbolIcons(Icons.Outlined.CreditCard, Icons.Filled.CreditCard),
        "cross.case" to SymbolIcons(Icons.Outlined.MedicalServices, Icons.Filled.MedicalServices),
        "cube.transparent" to SymbolIcons(Icons.Outlined.ViewInAr, Icons.Filled.ViewInAr),
        "cup.and.saucer" to SymbolIcons(Icons.Outlined.LocalCafe, Icons.Filled.LocalCafe),
        "dollarsign.circle" to SymbolIcons(Icons.Outlined.MonetizationOn, Icons.Filled.MonetizationOn),
        "dollarsign.gauge.chart.leftthird.topthird.rightthird" to SymbolIcons(Icons.Outlined.Speed, Icons.Filled.Speed),
        "dumbbell" to SymbolIcons(Icons.Outlined.FitnessCenter, Icons.Filled.FitnessCenter),
        "ellipsis" to SymbolIcons(Icons.Outlined.MoreHoriz, Icons.Filled.MoreHoriz),
        "ellipsis.circle" to SymbolIcons(Icons.Outlined.MoreHoriz, Icons.Filled.MoreHoriz),
        "envelope" to SymbolIcons(Icons.Outlined.Email, Icons.Filled.Email),
        "exclamationmark.circle" to SymbolIcons(Icons.Outlined.Error, Icons.Filled.Error),
        "exclamationmark.triangle" to SymbolIcons(Icons.Outlined.Warning, Icons.Filled.Warning),
        "figure.and.child.holdinghands" to SymbolIcons(Icons.Outlined.FamilyRestroom, Icons.Filled.FamilyRestroom),
        "film" to SymbolIcons(Icons.Outlined.Movie, Icons.Filled.Movie),
        "flag.checkered" to SymbolIcons(Icons.Outlined.SportsScore, Icons.Filled.SportsScore),
        "flag" to SymbolIcons(Icons.Outlined.Flag, Icons.Filled.Flag),
        "fork.knife" to SymbolIcons(Icons.Outlined.Restaurant, Icons.Filled.Restaurant),
        "gamecontroller" to SymbolIcons(Icons.Outlined.SportsEsports, Icons.Filled.SportsEsports),
        "gearshape" to SymbolIcons(Icons.Outlined.Settings, Icons.Filled.Settings),
        "gift" to SymbolIcons(Icons.Outlined.CardGiftcard, Icons.Filled.CardGiftcard),
        "globe" to SymbolIcons(Icons.Outlined.Language, Icons.Filled.Language),
        "graduationcap" to SymbolIcons(Icons.Outlined.School, Icons.Filled.School),
        "hand.raised" to SymbolIcons(Icons.Outlined.PanTool, Icons.Filled.PanTool),
        "house" to SymbolIcons(Icons.Outlined.Home, Icons.Filled.Home),
        "icloud" to SymbolIcons(Icons.Outlined.Cloud, Icons.Filled.Cloud),
        "info.circle" to SymbolIcons(Icons.Outlined.Info, Icons.Filled.Info),
        "iphone" to SymbolIcons(Icons.Outlined.Smartphone, Icons.Filled.Smartphone),
        "iphone.gen3" to SymbolIcons(Icons.Outlined.Smartphone, Icons.Filled.Smartphone),
        "laptopcomputer" to SymbolIcons(Icons.Outlined.Laptop, Icons.Filled.Laptop),
        "list.bullet.rectangle" to SymbolIcons(Icons.Outlined.ListAlt, Icons.Filled.ListAlt),
        "list.number" to SymbolIcons(Icons.Outlined.FormatListNumbered, Icons.Filled.FormatListNumbered),
        "lock" to SymbolIcons(Icons.Outlined.Lock, Icons.Filled.Lock),
        "lock.shield" to SymbolIcons(Icons.Outlined.Security, Icons.Filled.Security),
        "magnifyingglass" to SymbolIcons(Icons.Outlined.Search, Icons.Filled.Search),
        "mappin.and.ellipse" to SymbolIcons(Icons.Outlined.PinDrop, Icons.Filled.PinDrop),
        "minus" to SymbolIcons(Icons.Outlined.Remove, Icons.Filled.Remove),
        "number" to SymbolIcons(Icons.Outlined.Tag, Icons.Filled.Tag),
        "pencil" to SymbolIcons(Icons.Outlined.Edit, Icons.Filled.Edit),
        "percent" to SymbolIcons(Icons.Outlined.Percent, Icons.Filled.Percent),
        "person.2" to SymbolIcons(Icons.Outlined.Group, Icons.Filled.Group),
        "person.badge.plus" to SymbolIcons(Icons.Outlined.PersonAdd, Icons.Filled.PersonAdd),
        "person" to SymbolIcons(Icons.Outlined.Person, Icons.Filled.Person),
        "phone" to SymbolIcons(Icons.Outlined.Call, Icons.Filled.Call),
        "photo" to SymbolIcons(Icons.Outlined.Photo, Icons.Filled.Photo),
        "plus" to SymbolIcons(Icons.Outlined.Add, Icons.Filled.Add),
        "questionmark.circle" to SymbolIcons(Icons.Outlined.HelpOutline, Icons.Filled.HelpOutline),
        "questionmark" to SymbolIcons(Icons.Outlined.QuestionMark, Icons.Filled.QuestionMark),
        "square.grid.2x2" to SymbolIcons(Icons.Outlined.GridView, Icons.Filled.GridView),
        "star" to SymbolIcons(Icons.Outlined.Star, Icons.Filled.Star),
        "storefront" to SymbolIcons(Icons.Outlined.Storefront, Icons.Filled.Storefront),
        "text.alignleft" to SymbolIcons(Icons.Outlined.FormatAlignLeft, Icons.Filled.FormatAlignLeft),
        "textformat" to SymbolIcons(Icons.Outlined.TextFields, Icons.Filled.TextFields),
        "trash" to SymbolIcons(Icons.Outlined.Delete, Icons.Filled.Delete),
        "tray.and.arrow.down" to SymbolIcons(Icons.Outlined.Inbox, Icons.Filled.Inbox),
        "trophy" to SymbolIcons(Icons.Outlined.EmojiEvents, Icons.Filled.EmojiEvents),
        "tshirt" to SymbolIcons(Icons.Outlined.Checkroom, Icons.Filled.Checkroom),
        "wifi" to SymbolIcons(Icons.Outlined.Wifi, Icons.Filled.Wifi),
        "wrench.and.screwdriver" to SymbolIcons(Icons.Outlined.Build, Icons.Filled.Build),
        "xmark.circle" to SymbolIcons(Icons.Outlined.Cancel, Icons.Filled.Cancel),
        "pawprint" to SymbolIcons(Icons.Outlined.Pets, Icons.Filled.Pets),
        "xmark" to SymbolIcons(Icons.Outlined.Close, Icons.Filled.Close),
    )
}
