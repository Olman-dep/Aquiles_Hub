plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin { jvmToolchain(21) }

dependencies {
    implementation(project(":providers"))
}

application {
    mainClass.set("dev.agenthub.playground.MainKt")
}
