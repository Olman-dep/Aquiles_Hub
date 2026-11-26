rootProject.name = "agent-hub"

pluginManagement {
    repositories { gradlePluginPortal(); mavenCentral() }
}
dependencyResolutionManagement {
    repositories { mavenCentral() }
}

include(":core", ":providers", ":playground")
