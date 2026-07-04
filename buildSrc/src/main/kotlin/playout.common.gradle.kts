import com.ncorti.ktfmt.gradle.TrailingCommaManagementStrategy

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.ncorti.ktfmt.gradle")
}

extensions.create<PlayoutExtension>("playout")

kotlin {
    explicitApi()
    jvmToolchain(25)
}

ktfmt {
    kotlinLangStyle()
    trailingCommaManagementStrategy = TrailingCommaManagementStrategy.COMPLETE
}

repositories {
    mavenCentral()
}
