plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
}

dependencies {

    commonMainApi(projects.lib.kotlin)
    commonMainApi(projects.domain.objects)

    commonMainApi(libs.kotlin.stdlib)
    commonMainApi(libs.kotlinx.coroutines.swing)

    commonTestImplementation(libs.kotlin.test)
}
