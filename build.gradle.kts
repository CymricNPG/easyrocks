plugins {
    kotlin("jvm") version "1.9.24"
}

group = "net.npg.rocks"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.alibaba:fastjson:2.0.52")
    implementation("org.rocksdb:rocksdbjni:9.4.0")

    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
}

tasks.test {
    useJUnitPlatform()
}


kotlin {
    jvmToolchain(21)
}