
plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.2"
    id("java")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation ("org.projectlombok:lombok")
    annotationProcessor ("org.projectlombok:lombok")
    //Circuit Breaker
    implementation ("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
    //caching
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.6")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    //openapi
    implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    implementation ("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter:1.20.3")
    testImplementation("org.testcontainers:mongodb:1.20.3")
    testImplementation ("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("io.rest-assured:rest-assured:5.5.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("jpackageJar") {
    dependsOn(tasks.named("bootJar"))
}
