plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

allprojects {
    group = property("GROUP") as String
    version = property("VERSION") as String

    repositories {
        mavenCentral()
    }
}

subprojects {
    afterEvaluate {
        if (plugins.hasPlugin("org.jetbrains.kotlin.jvm")) {
            apply(plugin = "maven-publish")

            configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("maven") {
                        from(components["java"])

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
                }
            }
        }
    }
}
