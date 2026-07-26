plugins {
    id("playout.common")
    alias(libs.plugins.ktor)
}

playout { enableTests() }

application.mainClass = "me.lucyydotp.playout.controller.StandaloneKt"

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation:3.5.0")
    runtimeOnly(libs.logback)

    implementation(projects.common)
    implementation(libs.kotlinx.serialization.hocon)

    fun ktor(module: String) = implementation("io.ktor:ktor-$module")
    ktor("server-core")

    // FIXME: if we want graal then we need CIO, but that means losing HTTP/2.
    //  Is that really an issue given everything is localhost anyway?
    ktor("server-netty")
    ktor("server-content-negotiation")
    ktor("server-websockets")
    ktor("serialization-kotlinx-json")
    ktor("network")
}
