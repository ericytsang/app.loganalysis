plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
}

dependencies {

    commonMainApi(projects.lib.kotlin)
    commonMainApi(projects.domain.objects)

    commonMainImplementation(projects.domain.sqlite)

    commonTestImplementation(libs.kotlin.test)
}
