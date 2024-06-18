import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"
}

group = "com.anne"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("com.oracle.oci.sdk:oci-java-sdk-bom:3.34.1")
        mavenBom("com.oracle.oci.sdk:oci-java-sdk-common-httpclient-jersey:3.34.1")
    }
}

val kotlinCoroutinesVersion by extra { "1.9.22" }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.1")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("org.springframework.boot:spring-boot-starter-security:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-websocket:3.2.2")
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.2.2")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")

    implementation("org.slf4j:slf4j-api:2.0.12")
    testImplementation("org.slf4j:slf4j-simple:2.0.12")

    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")

    implementation("com.googlecode.json-simple:json-simple:1.1.1")

    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.4")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    runtimeOnly("com.mysql:mysql-connector-j:8.3.0")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.2")
    testRuntimeOnly("com.h2database:h2:2.1.214")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
