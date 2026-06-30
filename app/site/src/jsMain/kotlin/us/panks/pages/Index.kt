package us.panks.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.breakpoint.displayIfAtLeast
import com.varabyte.kobweb.silk.style.breakpoint.displayUntil
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.css.*
import us.panks.HeroGradientStyle
import us.panks.PageContainerStyle
import us.panks.components.layouts.PageLayoutData
import us.panks.data.ServiceLocator
import us.panks.toSitePalette

val FeatureCardStyle = CssStyle.base {
    Modifier
        .fillMaxWidth()
        .padding(1.25.cssRem)
        .borderRadius(0.75.cssRem)
        .border(1.px, LineStyle.Solid, colorMode.toSitePalette().borderColor)
        .backgroundColor(colorMode.toSitePalette().cardBackground)
}

@InitRoute
fun initHomePage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Home"))
}

@Composable
private fun FeatureCard(icon: String, title: String, description: String) {
    val sitePalette = ColorMode.current.toSitePalette()
    Column(FeatureCardStyle.toModifier().gap(0.5.cssRem)) {
        SpanText(icon, Modifier.fontSize(1.6.cssRem))
        SpanText(title, Modifier.fontSize(1.05.cssRem).fontWeight(600))
        SpanText(description, Modifier.fontSize(0.85.cssRem).color(sitePalette.mutedText).lineHeight(1.5))
    }
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun HomePage() {
    val sitePalette = ColorMode.current.toSitePalette()
    val loggedIn = ServiceLocator.auth.isLoggedIn()

    Column(
        PageContainerStyle.toModifier().gap(2.cssRem),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero
        Column(
            HeroGradientStyle.toModifier().gap(1.cssRem),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SpanText(
                "Practice Store",
                Modifier.fontSize(2.cssRem).fontWeight(700).color(Colors.White).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center)
            )
            SpanText(
                "Manage users and their orders with a clean, modern interface.",
                Modifier.fontSize(1.cssRem).color(Colors.White.copyf(alpha = 0.9f)).textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center)
            )

            // Buttons: Row on desktop, Column on mobile
            Column(
                Modifier.padding(top = 0.5.cssRem).gap(0.75.cssRem).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (loggedIn) {
                    Link(
                        "/users",
                        "View Users \u2192",
                        Modifier
                            .padding(0.75.cssRem, 1.5.cssRem)
                            .borderRadius(0.5.cssRem)
                            .backgroundColor(Colors.White)
                            .color(sitePalette.brand.primary)
                            .fontWeight(600)
                            .fontSize(0.95.cssRem)
                    )
                } else {
                    Link(
                        "/register",
                        "Get Started \u2192",
                        Modifier
                            .padding(0.75.cssRem, 1.5.cssRem)
                            .borderRadius(0.5.cssRem)
                            .backgroundColor(Colors.White)
                            .color(sitePalette.brand.primary)
                            .fontWeight(600)
                            .fontSize(0.95.cssRem)
                            .fillMaxWidth()
                            .textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center)
                    )
                    Link(
                        "/login",
                        "Login",
                        Modifier
                            .padding(0.75.cssRem, 1.5.cssRem)
                            .borderRadius(0.5.cssRem)
                            .border(1.px, LineStyle.Solid, Colors.White.copyf(alpha = 0.5f))
                            .color(Colors.White)
                            .fontWeight(600)
                            .fontSize(0.95.cssRem)
                            .fillMaxWidth()
                            .textAlign(com.varabyte.kobweb.compose.css.TextAlign.Center)
                    )
                }
            }
        }

        // Feature cards: Row on desktop, Column on mobile
        Row(
            Modifier.fillMaxWidth().gap(1.cssRem).displayIfAtLeast(Breakpoint.MD)
        ) {
            FeatureCard("\uD83D\uDC64", "User Management", "Create, view, update and delete users with pagination and age filtering.")
            FeatureCard("\uD83D\uDED2", "Order Tracking", "Add and browse orders for each user with product, quantity and price.")
            FeatureCard("\uD83D\uDD10", "JWT Auth", "Secure token-based authentication with bcrypt password hashing.")
        }

        Column(
            Modifier.fillMaxWidth().gap(1.cssRem).displayUntil(Breakpoint.MD)
        ) {
            FeatureCard("\uD83D\uDC64", "User Management", "Create, view, update and delete users with pagination and age filtering.")
            FeatureCard("\uD83D\uDED2", "Order Tracking", "Add and browse orders for each user with product, quantity and price.")
            FeatureCard("\uD83D\uDD10", "JWT Auth", "Secure token-based authentication with bcrypt password hashing.")
        }
    }
}
