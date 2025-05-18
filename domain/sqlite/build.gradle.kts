import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.ksp)
    id("app.cash.sqldelight") version "2.1.0"
}

kotlin {
    jvm()

    sourceSets.commonMain.dependencies {
        api(projects.lib.kotlin)
        api(projects.domain.objects)
        implementation(libs.primitive.adapters)
        api("app.cash.sqldelight:coroutines-extensions:2.1.0")
    }

    sourceSets.commonTest.dependencies {
        implementation(libs.kotlin.test)
    }

    sourceSets.jvmMain.dependencies {
        implementation("app.cash.sqldelight:sqlite-driver:2.1.0")
    }
}

sqldelight {
    databases {
        create("SqlDelightDatabase") {
            packageName = "com.github.ericytsang.service.sqlite"
        }
    }
}
