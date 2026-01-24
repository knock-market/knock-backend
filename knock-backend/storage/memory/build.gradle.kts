dependencies {
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	compileOnly("com.fasterxml.jackson.core:jackson-databind")

	api("org.springframework.boot:spring-boot-starter")
	api("org.springframework.boot:spring-boot-starter-web")
	api("org.springframework.security:spring-security-core")
	api("org.springframework.boot:spring-boot-starter-data-redis")
	api("org.springframework.session:spring-session-data-redis")
}
