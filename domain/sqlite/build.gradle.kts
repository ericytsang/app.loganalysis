plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    jvm()
}

dependencies {

    commonMainApi(projects.lib.kotlin)
    commonMainApi(projects.domain.objects)

    commonMainApi(libs.kotlin.stdlib)
    commonMainApi(libs.kotlinx.coroutines.swing)

    // room SQLite library
    commonMainImplementation(libs.androidx.room.gradle)
    commonMainImplementation(libs.androidx.room.compiler)
    commonMainImplementation(libs.androidx.sqlite.sqliteBundled)
    commonMainApi(libs.androidx.room.runtime)
    commonMainApi(libs.androidx.room.paging)

    commonTestImplementation(libs.kotlin.test)
}
