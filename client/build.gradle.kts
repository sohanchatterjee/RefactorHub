import com.moowork.gradle.node.yarn.YarnTask

plugins {
    id("com.github.node-gradle.node") version "2.1.1"
}

node {
    download = true
}

tasks {
    register<YarnTask>("generate") {
        dependsOn("yarn")
        args = listOf("generate")
    }
}
