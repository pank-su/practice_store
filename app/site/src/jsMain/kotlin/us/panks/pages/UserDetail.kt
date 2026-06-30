package us.panks.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.PageContext
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.breakpoint.displayIfAtLeast
import com.varabyte.kobweb.silk.style.breakpoint.displayUntil
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.css.cssRem
import us.panks.*
import us.panks.components.layouts.PageLayoutData
import us.panks.data.ServiceLocator
import us.panks.toSitePalette
import us.panks.ui.components.StyledInput
import us.panks.ui.userdetail.UserDetailViewModel

@InitRoute
fun initUserDetailPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("User Detail"))
}

@Page("/users/{id}")
@Layout(".components.layouts.PageLayout")
@Composable
fun UserDetailPage(ctx: PageContext) {
    val scope = rememberCoroutineScope()
    val sitePalette = ColorMode.current.toSitePalette()
    val cm = ColorMode.current
    val vm = remember { UserDetailViewModel() }

    val userId = ctx.route.params["id"]?.toIntOrNull() ?: 0

    LaunchedEffect(userId) {
        if (!ServiceLocator.auth.isLoggedIn()) {
            ctx.router.tryRoutingTo("/login")
        } else if (userId > 0) {
            vm.load(scope, userId) { ctx.router.tryRoutingTo("/login") }
        }
    }

    Column(
        PageContainerStyle.toModifier().gap(1.5.cssRem),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth()) {
            Link("\u2190 /users", "/users", Modifier.fontSize(0.9.cssRem).color(sitePalette.mutedText))
        }

        if (vm.loading) {
            Column(CardStyle.toModifier(), horizontalAlignment = Alignment.CenterHorizontally) {
                SpanText("Loading...", Modifier.color(sitePalette.mutedText))
            }
            return@Column
        }

        vm.error?.let {
            SpanText(it, ErrorBannerStyle.toModifier())
            return@Column
        }

        vm.user?.let { user ->
            // User info card
            Column(CardStyle.toModifier().gap(1.cssRem)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SpanText(
                        "${user.name.firstOrNull() ?: "U"}",
                        Modifier
                            .fontSize(1.5.cssRem)
                            .fontWeight(700)
                            .color(sitePalette.brand.primary)
                            .padding(0.6.cssRem)
                            .borderRadius(0.6.cssRem)
                            .backgroundColor(sitePalette.brand.primary.toRgb().copyf(alpha = 0.12f))
                    )
                    Column(
                        Modifier.padding(left = 1.cssRem).gap(0.2.cssRem)
                            .fillMaxWidth()
                    ) {
                        SpanText(user.name, HeadlineTextStyle.toModifier().fontSize(1.3.cssRem))
                        SpanText(user.email, Modifier.fontSize(0.875.cssRem).color(sitePalette.mutedText))
                    }
                    Spacer()
                    SpanText(
                        "${user.age} yrs",
                        Modifier.padding(0.2.cssRem, 0.6.cssRem).borderRadius(1.cssRem)
                            .backgroundColor(cm.toSitePalette().brand.primary.toRgb().copyf(alpha = 0.15f))
                            .color(cm.toSitePalette().brand.primary).fontSize(0.75.cssRem).fontWeight(600)
                    )
                }

                Button(
                    onClick = { vm.delete(scope, user.id) { ctx.router.tryRoutingTo("/users") } },
                    variant = DangerButtonVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) { SpanText("Delete User") }
            }

            // Orders title
            SpanText("Orders", SectionTitleStyle.toModifier().fillMaxWidth().textAlign(com.varabyte.kobweb.compose.css.TextAlign.Start))

            // Add order form
            Column(CardStyle.toModifier().gap(1.cssRem)) {
                SpanText("Add Order", Modifier.fontWeight(600))
                Column(Modifier.gap(0.4.cssRem).fillMaxWidth()) {
                    SpanText("Product", FormLabelStyle.toModifier())
                    StyledInput(vm.product, onTextChange = { vm.product = it }, placeholder = "Laptop")
                }

                // Qty + Price: Row on desktop, Column on mobile
                Row(
                    Modifier.fillMaxWidth().gap(1.cssRem).displayIfAtLeast(Breakpoint.SM)
                ) {
                    Column(Modifier.gap(0.4.cssRem).weight(1f)) {
                        SpanText("Quantity", FormLabelStyle.toModifier())
                        StyledInput(vm.quantity, onTextChange = { vm.quantity = it.filter { c -> c.isDigit() } }, placeholder = "1")
                    }
                    Column(Modifier.gap(0.4.cssRem).weight(1f)) {
                        SpanText("Price", FormLabelStyle.toModifier())
                        StyledInput(vm.price, onTextChange = { vm.price = it.filter { c -> c.isDigit() || c == '.' } }, placeholder = "1200.50")
                    }
                }
                Column(
                    Modifier.fillMaxWidth().gap(1.cssRem).displayUntil(Breakpoint.SM)
                ) {
                    Column(Modifier.gap(0.4.cssRem).fillMaxWidth()) {
                        SpanText("Quantity", FormLabelStyle.toModifier())
                        StyledInput(vm.quantity, onTextChange = { vm.quantity = it.filter { c -> c.isDigit() } }, placeholder = "1")
                    }
                    Column(Modifier.gap(0.4.cssRem).fillMaxWidth()) {
                        SpanText("Price", FormLabelStyle.toModifier())
                        StyledInput(vm.price, onTextChange = { vm.price = it.filter { c -> c.isDigit() || c == '.' } }, placeholder = "1200.50")
                    }
                }

                vm.orderError?.let { SpanText(it, ErrorBannerStyle.toModifier()) }
                Button(
                    onClick = { vm.createOrder(scope, user.id) },
                    enabled = !vm.orderLoading,
                    variant = PrimaryButtonVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) { SpanText(if (vm.orderLoading) "Adding..." else "Add Order") }
            }

            // Orders list
            if (vm.orders.isEmpty()) {
                Column(CardStyle.toModifier(), horizontalAlignment = Alignment.CenterHorizontally) {
                    SpanText("No orders yet", Modifier.color(sitePalette.mutedText))
                }
            } else {
                vm.orders.forEach { order ->
                    Column(CardStyle.toModifier().gap(0.3.cssRem)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            SpanText(order.product, Modifier.fontWeight(600).fontSize(1.cssRem))
                            Spacer()
                            SpanText("$${order.price}", Modifier.fontWeight(600).color(sitePalette.brand.primary))
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            SpanText("x${order.quantity}", BadgeStyle.toModifier())
                            Spacer()
                            SpanText(order.createdAt, Modifier.fontSize(0.75.cssRem).color(sitePalette.mutedText))
                        }
                    }
                }
            }
        }
    }
}
