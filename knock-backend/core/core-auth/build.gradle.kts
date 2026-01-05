dependencies {
	implementation(project(":storage:db-core"))
	implementation(project(":storage:memory"))

	api("org.springframework.boot:spring-boot-starter-security")
	api("org.springframework.boot:spring-boot-starter-web")
}
