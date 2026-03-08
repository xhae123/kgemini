plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.maven.publish) apply false
}

allprojects {
    group = property("GROUP") as String
    version = property("VERSION") as String

    repositories {
        mavenCentral()
    }
}
