plugins {
    // Application plugin: https://docs.gradle.org/current/userguide/application_plugin.html
    application

    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"

    val kotlinVersion = "2.0.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    // No-arg constructors for JPA entities; needed for JMS queue consumption.
    kotlin("plugin.jpa") version kotlinVersion
}

java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    // Spring Framework and Spring Boot.
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")

    // TODO this is the source of the build failure:
    implementation("com.expediagroup:graphql-kotlin-spring-server:8.1.0")

    implementation("com.graphql-java:graphql-java-extended-scalars:22.0")

    // Additional libraries

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")

    // Spring Framework and Spring Boot.
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-api-logs:1.26.0-alpha")

    // OpenAPI.
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.springdoc:springdoc-openapi-starter-common:2.6.0")

    // Only needed at runtime for the codegen to be able to run in CI.
    runtimeOnly("com.h2database:h2:2.3.232")
}
