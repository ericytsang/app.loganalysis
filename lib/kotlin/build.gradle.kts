plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
}

dependencies {

    commonMainApi(libs.kotlin.stdlib)
    commonMainApi(libs.kotlinx.coroutines.swing)

    commonTestImplementation(libs.kotlin.test)
}
