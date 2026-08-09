import sun.jvmstat.monitor.MonitoredVmUtil.mainClass

plugins {
    application
    id("playout.common")
    alias(libs.plugins.graalvm)
}

application { mainClass = "me.lucyydotp.playout.desktop.MainKt" }

dependencies { implementation(projects.controller) }
