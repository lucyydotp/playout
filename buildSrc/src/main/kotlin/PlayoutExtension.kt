import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.named

open class PlayoutExtension {
    fun Project.enableTests() {
        tasks.named<Test>("test") {
            useJUnitPlatform()
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        fun lib(name: String) = libs.findLibrary(name).orElseThrow()

        dependencies {
            "testImplementation"(kotlin("test"))
            "testImplementation"(lib("kotlinx-coroutines-test"))
            "testImplementation"(lib("mockk"))
        }
    }
}
