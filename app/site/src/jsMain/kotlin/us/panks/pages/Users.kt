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
import com.varabyte.kobweb.core.data.add
import com.varabyte.kobweb.core.init.InitRoute
import com.varabyte.kobweb.core.init.InitRouteContext
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.breakpoint.displayIfAtLeast
import com.varabyte.kobweb.silk.style.breakpoint.displayUntil
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.css.cssRem
import org.jetbrains.compose.web.css.px
import us.panks.*
import us.panks.components.layouts.PageLayoutData
import us.panks.data.ServiceLocator
import us.panks.toSitePalette
import us.panks.ui.components.StyledInput
import us.panks.ui.users.UsersViewModel

@InitRoute
fun initUsersPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Users"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun UsersPage() {
    val ctx = rememberPageContext()
    val scope = rememberCoroutineScope()
    val sitePalette = ColorMode.current.toSitePalette()
    val vm = remember { UsersViewModel() }

    LaunchedEffect(Unit) {
        if (!ServiceLocator.auth.isLoggedIn()) {
            ctx.router.tryRoutingTo("/login")
        } else {
            vm.load(scope, 1) { ctx.router.tryRoutingTo("/login") }
        }
    }

    Column(
        PageContainerStyle.toModifier().gap(1.5.cssRem),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SpanText("Users", HeadlineTextStyle.toModifier().fontSize(1.75.cssRem))
            Spacer()
            if (vm.total > 0) {
                SpanText("${vm.total} total", BadgeStyle.toModifier())
            }
        }

        // Filter card — Row on desktop, Column on mobile
        Row(
            CardStyle.toModifier().gap(1.cssRem).displayIfAtLeast(Breakpoint.SM),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(Modifier.gap(0.3.cssRem)) {
                SpanText("Min age", FormLabelStyle.toModifier())
                StyledInput(vm.minAge, onTextChange = { vm.minAge = it.filter { c -> c.isDigit() } }, modifier = Modifier.width(7.cssRem), placeholder = "0")
            }
            Column(Modifier.gap(0.3.cssRem)) {
                SpanText("Max age", FormLabelStyle.toModifier())
                StyledInput(vm.maxAge, onTextChange = { vm.maxAge = it.filter { c -> c.isDigit() } }, modifier = Modifier.width(7.cssRem), placeholder = "99")
            }
            Spacer()
            Button(
                onClick = { vm.applyFilter(scope) { ctx.router.tryRoutingTo("/login") } },
                variant = PrimaryButtonVariant,
            ) { SpanText("Apply") }
        }

        Column(
            CardStyle.toModifier().gap(0.75.cssRem).displayUntil(Breakpoint.SM),
        ) {
            Column(Modifier.gap(0.3.cssRem).fillMaxWidth()) {
                SpanText("Min age", FormLabelStyle.toModifier())
                StyledInput(vm.minAge, onTextChange = { vm.minAge = it.filter { c -> c.isDigit() } }, placeholder = "0")
            }
            Column(Modifier.gap(0.3.cssRem).fillMaxWidth()) {
                SpanText("Max age", FormLabelStyle.toModifier())
                StyledInput(vm.maxAge, onTextChange = { vm.maxAge = it.filter { c -> c.isDigit() } }, placeholder = "99")
            }
            Button(
                onClick = { vm.applyFilter(scope) { ctx.router.tryRoutingTo("/login") } },
                variant = PrimaryButtonVariant,
                modifier = Modifier.fillMaxWidth(),
            ) { SpanText("Apply") }
        }

        vm.error?.let { SpanText(it, ErrorBannerStyle.toModifier()) }

        if (vm.loading) {
            Column(CardStyle.toModifier(), horizontalAlignment = Alignment.CenterHorizontally) {
                SpanText("Loading...", Modifier.color(sitePalette.mutedText))
            }
        } else if (vm.users.isEmpty()) {
            Column(CardStyle.toModifier(), horizontalAlignment = Alignment.CenterHorizontally) {
                SpanText("No users found", Modifier.color(sitePalette.mutedText))
            }
        } else {
            // Desktop table
            Column(
                CardStyle.toModifier().gap(0.cssRem).padding(0.px).displayIfAtLeast(Breakpoint.SM)
            ) {
                Row(
                    TableHeaderStyle.toModifier(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SpanText("ID", Modifier.width(3.cssRem))
                    SpanText("Name", Modifier.width(10.cssRem))
                    SpanText("Email", Modifier.width(16.cssRem))
                    SpanText("Age", Modifier.width(4.cssRem))
                }

                vm.users.forEach { user ->
                    Row(
                        TableRowStyle.toModifier(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SpanText("${user.id}", Modifier.width(3.cssRem).color(sitePalette.mutedText).fontSize(0.85.cssRem))
                        Link("/users/${user.id}", user.name, Modifier.width(10.cssRem).fontWeight(500))
                        SpanText(user.email, Modifier.width(16.cssRem).color(sitePalette.mutedText).fontSize(0.9.cssRem))
                        SpanText("${user.age}", Modifier.width(4.cssRem))
                    }
                }
            }

            // Mobile cards
            Column(
                Modifier.fillMaxWidth().gap(0.75.cssRem).displayUntil(Breakpoint.SM)
            ) {
                vm.users.forEach { user ->
                    Column(CardStyle.toModifier().gap(0.5.cssRem)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            SpanText(
                                "${user.id}",
                                Modifier.fontSize(0.75.cssRem).color(sitePalette.mutedText)
                            )
                            Spacer()
                            SpanText(
                                "${user.age} yrs",
                                Modifier.padding(0.15.cssRem, 0.5.cssRem).borderRadius(1.cssRem)
                                    .backgroundColor(sitePalette.brand.primary.toRgb().copyf(alpha = 0.15f))
                                    .color(sitePalette.brand.primary).fontSize(0.7.cssRem).fontWeight(600)
                            )
                        }
                        Link("/users/${user.id}", user.name, Modifier.fontWeight(600).fontSize(1.cssRem))
                        SpanText(user.email, Modifier.fontSize(0.85.cssRem).color(sitePalette.mutedText))
                    }
                }
            }

            // Pagination
            Row(
                Modifier.fillMaxWidth().padding(top = 0.5.cssRem).gap(1.cssRem),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { if (vm.page > 1) vm.load(scope, vm.page - 1) { ctx.router.tryRoutingTo("/login") } },
                    enabled = vm.page > 1,
                    variant = GhostButtonVariant,
                ) { SpanText("\u2190 Prev") }

                SpanText(
                    "Page ${vm.page} of ${if (vm.totalPages == 0L) 1 else vm.totalPages}",
                    Modifier.fontSize(0.9.cssRem).color(sitePalette.mutedText)
                )

                Button(
                    onClick = { if (vm.page < vm.totalPages) vm.load(scope, vm.page + 1) { ctx.router.tryRoutingTo("/login") } },
                    enabled = vm.page < vm.totalPages,
                    variant = GhostButtonVariant,
                ) { SpanText("Next \u2192") }
            }
        }
    }
}
