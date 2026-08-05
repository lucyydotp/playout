import io.clroot.gradle.bun.task.BunTask

plugins {
    alias(libs.plugins.bun)
}

bun {
    version = "1.3.14"
}

tasks.register<BunTask>("build") {
    dependsOn("biomeCheck")
    args("run", "build")
    inputs.dir("src")
    inputs.file("vite.config.ts")
    inputs.file("tsconfig.json")
    outputs.dir("build/dist")
}

tasks.register<BunTask>("viteDev") {
    outputs.upToDateWhen { false }
    args("run", "dev")
}

tasks.register<BunTask>("biomeCheck") {
    inputs.dir("src")
    args("run", "check")
}

tasks.register<BunTask>("biomeFormat") {
    inputs.dir("src")
    args("run", "format")
}

configurations.create("webBundle") {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    add("webBundle", tasks.named("build"))
}
