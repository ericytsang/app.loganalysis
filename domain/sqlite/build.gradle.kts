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
    commonMainApi(libs.androidx.room.compiler)
    commonMainApi(libs.androidx.room.runtime)
    commonMainApi(libs.androidx.room.paging)
    commonMainApi(libs.androidx.sqlite.sqliteBundled)

    commonTestImplementation(libs.kotlin.test)
}
