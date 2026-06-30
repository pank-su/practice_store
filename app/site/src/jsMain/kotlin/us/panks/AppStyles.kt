package us.panks

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.ScrollBehavior
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.forms.ButtonStyle
import com.varabyte.kobweb.silk.components.forms.ButtonVars
import com.varabyte.kobweb.silk.components.layout.HorizontalDividerStyle
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.addVariantBase
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.palette.toPalette
import com.varabyte.kobweb.silk.theme.modifyStyleBase
import org.jetbrains.compose.web.css.*

@InitSilk
fun initSiteStyles(ctx: InitSilkContext) {
    ctx.stylesheet.registerStyle("html") {
        cssRule(CSSMediaQuery.MediaFeature("prefers-reduced-motion", StylePropertyValue("no-preference"))) {
            Modifier.scrollBehavior(ScrollBehavior.Smooth)
        }
    }

    ctx.stylesheet.registerStyleBase("body") {
        Modifier
            .fontFamily(
                "-apple-system", "BlinkMacSystemFont", "Segoe UI", "Roboto", "Oxygen", "Ubuntu",
                "Cantarell", "Fira Sans", "Droid Sans", "Helvetica Neue", "sans-serif"
            )
            .fontSize(16.px)
            .lineHeight(1.6)
    }

    ctx.theme.modifyStyleBase(HorizontalDividerStyle) {
        Modifier.fillMaxWidth()
    }
}

// ── Text styles ──────────────────────────────────────────────

val HeadlineTextStyle = CssStyle.base {
    Modifier
        .fontSize(2.5.cssRem)
        .fontWeight(700)
        .textAlign(TextAlign.Start)
        .lineHeight(1.2)
}

val SectionTitleStyle = CssStyle.base {
    Modifier
        .fontSize(1.5.cssRem)
        .fontWeight(600)
        .textAlign(TextAlign.Start)
        .lineHeight(1.3)
}

val SubheadlineTextStyle = CssStyle.base {
    Modifier
        .fontSize(1.1.cssRem)
        .textAlign(TextAlign.Start)
        .color(colorMode.toPalette().color.toRgb().copyf(alpha = 0.7f))
}

// ── Card ─────────────────────────────────────────────────────

val CardStyle = CssStyle {
    base {
        Modifier
            .fillMaxWidth()
            .padding(1.5.cssRem)
            .borderRadius(0.75.cssRem)
            .border(1.px, LineStyle.Solid, colorMode.toSitePalette().borderColor)
            .backgroundColor(colorMode.toSitePalette().cardBackground)
            .boxShadow(
                blurRadius = 1.cssRem,
                spreadRadius = 0.px,
                color = Color.rgb(0x000000).copyf(alpha = 0.05f),
            )
    }
}

// ── Form label ───────────────────────────────────────────────

val FormLabelStyle = CssStyle.base {
    Modifier
        .fontSize(0.875.cssRem)
        .fontWeight(500)
        .color(colorMode.toPalette().color.toRgb().copyf(alpha = 0.7f))
}

// ── Text input ───────────────────────────────────────────────

@Composable
fun inputModifier(): Modifier {
    val palette = ColorMode.current.toSitePalette()
    return Modifier
        .fillMaxWidth()
        .padding(0.7.cssRem, 0.9.cssRem)
        .borderRadius(0.5.cssRem)
        .border(1.px, LineStyle.Solid, palette.borderColor)
        .backgroundColor(palette.cardBackground)
        .fontSize(0.95.cssRem)
        .color(ColorMode.current.toPalette().color)
        .outline(0.px, LineStyle.None, Color.rgb(0x000000))
}

// ── Error banner ─────────────────────────────────────────────

val ErrorBannerStyle = CssStyle.base {
    Modifier
        .fillMaxWidth()
        .padding(0.75.cssRem, 1.cssRem)
        .borderRadius(0.5.cssRem)
        .backgroundColor(colorMode.toSitePalette().danger.toRgb().copyf(alpha = 0.1f))
        .color(colorMode.toSitePalette().danger)
        .fontSize(0.875.cssRem)
}

// ── Table row ────────────────────────────────────────────────

val TableHeaderStyle = CssStyle.base {
    Modifier
        .fillMaxWidth()
        .padding(0.875.cssRem, 1.cssRem)
        .backgroundColor(colorMode.toSitePalette().nearBackground)
        .borderRadius(0.5.cssRem)
        .fontSize(0.75.cssRem)
        .fontWeight(600)
        .letterSpacing(0.05.cssRem)
        .color(colorMode.toSitePalette().mutedText)
}

val TableRowStyle = CssStyle.base {
    Modifier
        .fillMaxWidth()
        .padding(0.875.cssRem, 1.cssRem)
        .border(1.px, LineStyle.Solid, colorMode.toSitePalette().borderColor)
        .borderTop(0.px)
}

// ── Badge ────────────────────────────────────────────────────

val BadgeStyle = CssStyle.base {
    Modifier
        .padding(0.2.cssRem, 0.6.cssRem)
        .borderRadius(1.cssRem)
        .backgroundColor(colorMode.toSitePalette().brand.primary.toRgb().copyf(alpha = 0.15f))
        .color(colorMode.toSitePalette().brand.primary)
        .fontSize(0.75.cssRem)
        .fontWeight(600)
}

// ── Button variants ──────────────────────────────────────────

val PrimaryButtonVariant = ButtonStyle.addVariantBase {
    Modifier
        .setVariable(ButtonVars.BackgroundDefaultColor, colorMode.toSitePalette().brand.primary)
        .setVariable(ButtonVars.BackgroundHoverColor, colorMode.toSitePalette().brand.primaryHover)
        .color(Colors.White)
        .borderRadius(0.5.cssRem)
        .fontWeight(600)
        .padding(0.75.cssRem, 1.5.cssRem)
}

val DangerButtonVariant = ButtonStyle.addVariantBase {
    Modifier
        .setVariable(ButtonVars.BackgroundDefaultColor, colorMode.toSitePalette().danger)
        .setVariable(ButtonVars.BackgroundHoverColor, colorMode.toSitePalette().danger.toRgb().copyf(alpha = 0.85f))
        .color(Colors.White)
        .borderRadius(0.5.cssRem)
        .fontWeight(600)
}

val GhostButtonVariant = ButtonStyle.addVariantBase {
    Modifier
        .setVariable(ButtonVars.BackgroundDefaultColor, Colors.Transparent)
        .setVariable(ButtonVars.BackgroundHoverColor, colorMode.toSitePalette().nearBackground)
        .border(1.px, LineStyle.Solid, colorMode.toSitePalette().borderColor)
        .borderRadius(0.5.cssRem)
        .fontWeight(500)
}

// ── Hero ─────────────────────────────────────────────────────

val HeroGradientStyle = CssStyle.base {
    Modifier
        .fillMaxWidth()
        .padding(3.cssRem, 2.cssRem)
        .borderRadius(1.cssRem)
        .backgroundColor(colorMode.toSitePalette().brand.primary)
}

// ── Page container ───────────────────────────────────────────

val PageContainerStyle = CssStyle {
    base { Modifier.fillMaxWidth().gap(1.5.cssRem) }
    Breakpoint.MD { Modifier.maxWidth(52.cssRem) }
}

// ── Legacy button variants (used by IconButton widget) ───────

val CircleButtonVariant = ButtonStyle.addVariantBase {
    Modifier.padding(0.px).borderRadius(50.percent)
}

val UncoloredButtonVariant = ButtonStyle.addVariantBase {
    Modifier.setVariable(ButtonVars.BackgroundDefaultColor, Colors.Transparent)
}
