plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":lib-router-annotation"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.21-1.0.28")
    implementation("com.squareup:kotlinpoet:1.16.0")
}

kotlin {
    jvmToolchain(17)
}