dependencies {
    implementation("software.amazon.awssdk:s3:2.21.46")
    implementation(project(":core:core-enum"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
