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

    // We only need WebTestClient in tests.
    testImplementation("org.springframework:spring-webflux")

    // TODO this is the source of the build failure:
    implementation("com.expediagroup:graphql-kotlin-spring-server:8.0.0")
}
