plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
}

dependencies {
    commonMainApi(libs.kotlinx.coroutines.swing)
}
