plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(21)
    explicitApi()
}

dependencies {
    api(project(":kgemini-core"))
}

signing {
    useGpgCmd()
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(property("GROUP") as String, "kgemini-models", property("VERSION") as String)

    pom {
        name.set("kgemini-models")
        description.set("Model metadata catalog for kgemini (pricing, token limits)")
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
