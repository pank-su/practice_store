package us.panks

import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.color

class SitePalette(
    val nearBackground: Color,
    val cardBackground: Color,
    val borderColor: Color,
    val mutedText: Color,
    val brand: Brand,
    val danger: Color,
    val success: Color,
) {
    class Brand(
        val primary: Color,
        val primaryHover: Color,
        val accent: Color,
        val gradientStart: Color,
        val gradientEnd: Color,
    )
}

object SitePalettes {
    val light = SitePalette(
        nearBackground = Color.rgb(0xF4F6FA),
        cardBackground = Color.rgb(0xFFFFFF),
        borderColor = Color.rgb(0xE2E8F0),
        mutedText = Color.rgb(0x64748B),
        brand = SitePalette.Brand(
            primary = Color.rgb(0x6366F1),
            primaryHover = Color.rgb(0x4F46E5),
            accent = Color.rgb(0xEC4899),
            gradientStart = Color.rgb(0x6366F1),
            gradientEnd = Color.rgb(0x8B5CF6),
        ),
        danger = Color.rgb(0xEF4444),
        success = Color.rgb(0x22C55E),
    )
    val dark = SitePalette(
        nearBackground = Color.rgb(0x13171F),
        cardBackground = Color.rgb(0x1E293B),
        borderColor = Color.rgb(0x334155),
        mutedText = Color.rgb(0x94A3B8),
        brand = SitePalette.Brand(
            primary = Color.rgb(0x818CF8),
            primaryHover = Color.rgb(0x6366F1),
            accent = Color.rgb(0xF472B6),
            gradientStart = Color.rgb(0x818CF8),
            gradientEnd = Color.rgb(0xA78BFA),
        ),
        danger = Color.rgb(0xF87171),
        success = Color.rgb(0x4ADE80),
    )
}

fun ColorMode.toSitePalette(): SitePalette {
    return when (this) {
        ColorMode.LIGHT -> SitePalettes.light
        ColorMode.DARK -> SitePalettes.dark
    }
}

@InitSilk
fun initTheme(ctx: InitSilkContext) {
    ctx.theme.palettes.light.background = Color.rgb(0xF8FAFC)
    ctx.theme.palettes.light.color = Color.rgb(0x0F172A)
    ctx.theme.palettes.dark.background = Color.rgb(0x0F172A)
    ctx.theme.palettes.dark.color = Color.rgb(0xF1F5F9)
}
