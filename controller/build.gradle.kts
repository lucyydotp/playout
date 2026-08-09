import io.ktor.plugin.features.DockerImageRegistry

plugins {
    id("playout.common")
    alias(libs.plugins.ktor)
}

playout { enableTests() }

application.mainClass = "me.lucyydotp.playout.controller.StandaloneKt"

if (System.getenv("GITHUB_ACTIONS") == "true") {
    val parsedTag =
        Regex("v(\\d+)\\.(\\d+)\\.(\\d+)").find(System.getenv("GITHUB_REF_NAME"))?.groupValues

    val tags =
        if (parsedTag != null) {
            setOf(
                parsedTag[1],
                "${parsedTag[1]}.${parsedTag[2]}",
                "${parsedTag[1]}.${parsedTag[2]}.${parsedTag[3]}",
            )
        } else {
            setOf(System.getenv("GITHUB_REF_NAME"))
        }

    ktor.docker {
        externalRegistry =
            DockerImageRegistry.externalRegistry(
                providers.environmentVariable("GITHUB_ACTOR"),
                providers.environmentVariable("GITHUB_TOKEN"),
                hostname = provider { "ghcr.io" },
                project = provider { "playout" },
                namespace = provider { "lucyydotp" },
            )
        imageTag = System.getenv("GITHUB_SHA").take(8)
    }

    // Ktor can't handle multiple tags, so add them ourselves
    jib.to.tags.addAll(tags)
}

val frontend = configurations.create("frontend")

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

    frontend(project(":frontend", configuration = "webBundle"))
}

tasks {
    processResources { from(frontend) { into("frontend") } }
    withType<JavaExec> { workingDir = file("run") }
}
