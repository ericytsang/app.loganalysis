plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
}

dependencies {
    commonMainApi(projects.lib.kotlin)
    commonTestImplementation(libs.kotlin.test)
}
