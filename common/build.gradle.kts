plugins { id("playout.common") }

playout { enableTests() }

dependencies { api(libs.kotlinx.serialization.json) }
