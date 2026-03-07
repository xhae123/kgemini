plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "kgemini"

include("kgemini-core", "kgemini-models", "kgemini-bom")
