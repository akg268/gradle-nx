plugins {
    id("dev.nx.gradle.project-graph") version("0.1.25")
    base
}

allprojects {
    apply(plugin = "dev.nx.gradle.project-graph")
    group = "dev.example"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
