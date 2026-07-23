plugins {
	java
	id("org.springframework.boot") version "3.5.15"
	id("io.spring.dependency-management") version "1.1.7"
	// id("org.asciidoctor.jvm.convert") version "4.0.3"
}

description = "Authentication and Authorization Service for Bakery"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenLocal()
	mavenCentral()
	maven {
		name = "GitHubPackages"
		url = uri("https://maven.pkg.github.com/amankrmj09/bakery-common-libs")
		credentials {
			username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
			password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
		}
	}
}

// extra["snippetsDir"] = file("build/generated-snippets")
val springCloudVersion by extra("2025.0.3")

dependencies {
	// 1. Shared Custom Libraries
	implementation("org.blubugtech.com:common-libs:2.2.1")

	// 2. Spring Boot Core & Web
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")

	// 3. Spring Cloud & Discovery
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation("org.springframework.cloud:spring-cloud-starter-config")

	// 4. Data & Persistence
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")

	// 5. Messaging & Event Driven
	implementation("org.springframework.kafka:spring-kafka")

	// 6. Security
	implementation("org.springframework.boot:spring-boot-starter-security")

	// 7. Third-Party Utilities (Jackson, AWS, etc.)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.8.17")
	implementation("io.swagger.core.v3:swagger-annotations:2.2.52")
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	implementation("org.bouncycastle:bcprov-jdk18on:1.84")

	// 8. Tooling & Lombok
	compileOnly("org.projectlombok:lombok")
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
	annotationProcessor("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.springframework.boot:spring-boot-docker-compose")
	// runtimeOnly("io.micrometer:micrometer-registry-prometheus")

	// 9. Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("com.h2database:h2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// testImplementation("org.springframework.boot:spring-boot-testcontainers")
	// testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
	// testImplementation("org.testcontainers:junit-jupiter")
	// testImplementation("org.testcontainers:postgresql")
}
dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// tasks.test {
// 	outputs.dir(project.extra["snippetsDir"]!!)
// }
//
// tasks.asciidoctor {
// 	inputs.dir(project.extra["snippetsDir"]!!)
// 	dependsOn(tasks.test)
// }
