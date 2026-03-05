plugins {
    `java-platform`
}

dependencies {
    constraints {
        api(project(":kgemini-core"))
        api(project(":kgemini-models"))
    }
}
