plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.gamehub"
version = "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")
}

tasks.bootJar {
    archiveFileName = "redis-game-hub.jar"
}

tasks.jar {
    enabled = false
}

// Redis-окружение для запуска приложения из IDE
tasks.register<Exec>("composeUp") {
    group = "docker"
    commandLine("docker", "compose", "up", "-d", "--wait")
}

tasks.register<Exec>("composeDown") {
    group = "docker"
    commandLine("docker", "compose", "--profile", "app", "down")
}
