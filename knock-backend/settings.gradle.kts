pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings
    val asciidoctorConvertVersion: String by settings
    val springJavaFormatVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.springframework.boot" -> useVersion(springBootVersion)
                "io.spring.dependency-management" -> useVersion(springDependencyManagementVersion)
                "org.asciidoctor.jvm.convert" -> useVersion(asciidoctorConvertVersion)
                "io.spring.javaformat" -> useVersion(springJavaFormatVersion)
            }
        }
    }
}

rootProject.name = "knock-backend"

include (
    "core:core-api",
    "core:core-enum",
    "core:core-auth",
    "storage:db-core",
    "storage:memory",
    "support:logging",
    "support:monitoring",
    "tests:api-docs",
    "clients:client-example",
    "infra:s3"
)
