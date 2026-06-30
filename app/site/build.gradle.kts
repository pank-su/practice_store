import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kobweb.application)
    alias(libs.plugins.kobwebx.markdown)
    alias(libs.plugins.openapi.generator)
}

group = "us.panks"
version = "1.0-SNAPSHOT"

kobweb {
    app {
        index {
            description.set("Practice Store — users and orders management")
        }
    }
}

val generatedStoreApiDir = layout.buildDirectory.dir("generated/openapi")

tasks.register<GenerateTask>("generateStoreApi") {
    group = "openapi"
    description = "Generate a Kotlin Multiplatform Ktor client from docs/swagger.json"

    generatorName.set("kotlin")
    library.set("multiplatform")
    inputSpec.set(layout.projectDirectory.file("../../docs/swagger.json").asFile.absolutePath)
    outputDir.set(generatedStoreApiDir.get().asFile.absolutePath)

    packageName.set("us.panks.generated.store")
    apiPackage.set("us.panks.generated.store.api")
    modelPackage.set("us.panks.generated.store.model")
    invokerPackage.set("us.panks.generated.store.client")

    configOptions.set(
        mapOf(
            "dateLibrary" to "kotlinx-datetime",
            "useCoroutines" to "true",
        )
    )
}

kotlin {
    configAsKobwebApplication("panks")

    sourceSets {
        jsMain {
            kotlin.srcDir(generatedStoreApiDir.map { it.dir("src/commonMain/kotlin") })

            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.html.core)
                implementation(libs.kobweb.core)
                implementation(libs.kobweb.silk)
                implementation(libs.silk.icons.fa)
                implementation(libs.kobwebx.markdown)
                implementation(libs.kobwebx.serialization.kotlinx)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.js)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
    }
}

tasks.named("compileKotlinJs") {
    dependsOn("generateStoreApi")
}

tasks.matching { it.name == "kspKotlinJs" }.configureEach {
    dependsOn("generateStoreApi")
}
