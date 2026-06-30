package us.panks.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
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
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.css.cssRem
import us.panks.CardStyle
import us.panks.ErrorBannerStyle
import us.panks.FormLabelStyle
import us.panks.HeadlineTextStyle
import us.panks.PageContainerStyle
import us.panks.PrimaryButtonVariant
import us.panks.components.layouts.PageLayoutData
import us.panks.toSitePalette
import us.panks.ui.components.StyledInput
import us.panks.ui.login.LoginViewModel

@InitRoute
fun initLoginPage(ctx: InitRouteContext) {
    ctx.data.add(PageLayoutData("Login"))
}

@Page
@Layout(".components.layouts.PageLayout")
@Composable
fun LoginPage() {
    val ctx = rememberPageContext()
    val scope = rememberCoroutineScope()
    val vm = remember { LoginViewModel() }

    Column(
        PageContainerStyle.toModifier(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            CardStyle.toModifier().gap(1.5.cssRem).maxWidth(26.cssRem),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SpanText("Welcome back", HeadlineTextStyle.toModifier().fontSize(1.75.cssRem))
            SpanText("Sign in to your account", Modifier.fontSize(0.9.cssRem).color(ColorMode.current.toSitePalette().mutedText))

            Column(Modifier.fillMaxWidth().gap(1.cssRem)) {
                Column(Modifier.gap(0.4.cssRem).fillMaxWidth()) {
                    SpanText("Email", FormLabelStyle.toModifier())
                    StyledInput(vm.email, onTextChange = { vm.email = it }, placeholder = "you@example.com")
                }
                Column(Modifier.gap(0.4.cssRem).fillMaxWidth()) {
                    SpanText("Password", FormLabelStyle.toModifier())
                    StyledInput(vm.password, onTextChange = { vm.password = it }, password = true, placeholder = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022")
                }

                vm.error?.let { SpanText(it, ErrorBannerStyle.toModifier()) }

                Button(
                    onClick = { vm.login(scope) { ctx.router.tryRoutingTo("/users") } },
                    enabled = !vm.loading,
                    variant = PrimaryButtonVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    SpanText(if (vm.loading) "Signing in..." else "Sign in")
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    SpanText("No account? ", Modifier.fontSize(0.9.cssRem).color(ColorMode.current.toSitePalette().mutedText))
                    Link("/register", "Register", Modifier.fontSize(0.9.cssRem).fontWeight(500))
                }
            }
        }
    }
}
