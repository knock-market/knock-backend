import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    id("java-library")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
    id("io.spring.javaformat") apply false
    id("org.asciidoctor.jvm.convert") apply false
    id("org.sonarqube") version "7.1.0.6387"
}

apply(from = "lint.gradle")

sonar {
    properties {
        property("sonar.projectKey", "knock-market_knock-backend")
        property("sonar.organization", "knock-market")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            subprojects
                .filter { it.file("src/test").exists() }
                .joinToString(",") {
                    it.layout.buildDirectory.file("reports/jacoco/test/jacocoTestReport.xml").get().asFile.absolutePath
                }
        )
    }
}

allprojects {
    group = "${property("projectGroup")}"
    version = "${property("applicationVersion")}"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "jacoco")

    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudDependenciesVersion")}")
        }
    }

    dependencies {
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    tasks.named("bootJar") {
        enabled = false
    }

    tasks.named("jar") {
        enabled = true
    }

    tasks.named("bootRun") {
        enabled = false
    }

    java.sourceCompatibility = JavaVersion.VERSION_21

    tasks.withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
        options.release.set(21)
    }

    tasks.register<Test>("unitTest") {
        group = "verification"
        useJUnitPlatform {
            excludeTags("develop", "context", "restdocs")
        }
    }

    tasks.register<Test>("contextTest") {
        group = "verification"
        useJUnitPlatform {
            includeTags("context")
        }
    }

    val restDocsTest = tasks.register<Test>("restDocsTest") {
        group = "verification"
        useJUnitPlatform {
            includeTags("restdocs")
        }
    }

    tasks.register<Test>("developTest") {
        group = "verification"
        useJUnitPlatform {
            includeTags("develop")
        }
    }

    tasks.test {
        useJUnitPlatform {
            excludeTags("develop", "restdocs")
        }
    }

    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn(tasks.test, restDocsTest)
        executionData(fileTree(layout.buildDirectory) {
            include("jacoco/test.exec", "jacoco/restDocsTest.exec")
        })
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

}

tasks.named("sonar") {
    dependsOn(subprojects.map { it.tasks.named("jacocoTestReport") })
}
