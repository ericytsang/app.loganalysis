plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm()

    compilerOptions {
        freeCompilerArgs.add("-Xwhen-guards")
    }

    sourceSets.commonMain.dependencies {

        implementation(projects.lib.kotlin)
        implementation(projects.lib.logcatFilterParser)
        implementation(projects.domain.objects)
        implementation(projects.domain.repo)
        implementation(projects.domain.appinfo)

        implementation("sh.calvin.reorderable:reorderable:2.5.1")

        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material)
        implementation(compose.ui)
        implementation(compose.components.resources)
        implementation(compose.components.uiToolingPreview)
        implementation(libs.androidx.lifecycle.viewmodel)
        implementation(libs.androidx.lifecycle.runtime.compose)
    }

    sourceSets.jvmMain.dependencies {
        implementation(compose.desktop.currentOs)
        implementation(libs.kotlinx.coroutines.swing)
    }
}

compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"
        version = "0.0.1"
    }
}
