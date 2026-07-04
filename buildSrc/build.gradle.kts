plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

fun DependencyHandler.plugin(plugin: Provider<PluginDependency>) {
    implementation(plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
}


dependencies {
    plugin(libs.plugins.kotlin.jvm)
    plugin(libs.plugins.kotlin.serialization)
    plugin(libs.plugins.ktfmt)
}
