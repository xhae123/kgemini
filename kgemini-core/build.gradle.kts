plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
    signing
}

kotlin {
    jvmToolchain(21)
    explicitApi()
}

dependencies {
    api(libs.kotlinx.serialization.json)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

signing {
    useGpgCmd()
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(property("GROUP") as String, "kgemini-core", property("VERSION") as String)

    pom {
        name.set(property("POM_NAME") as String)
        description.set(property("POM_DESCRIPTION") as String)
        url.set(property("POM_URL") as String)
        licenses {
            license {
                name.set(property("POM_LICENSE_NAME") as String)
                url.set(property("POM_LICENSE_URL") as String)
            }
        }
        developers {
            developer {
                id.set(property("POM_DEVELOPER_ID") as String)
                name.set(property("POM_DEVELOPER_NAME") as String)
            }
        }
        scm {
            url.set(property("POM_SCM_URL") as String)
            connection.set(property("POM_SCM_CONNECTION") as String)
            developerConnection.set(property("POM_SCM_DEV_CONNECTION") as String)
        }
    }
}
