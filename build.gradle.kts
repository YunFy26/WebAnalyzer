plugins {
    java
    application
}

repositories {
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
}

group = "org.example"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("org.example.MyMain")
}

val taieVersion = "0.5.1-SNAPSHOT" // or the latest version "0.5.1-SNAPSHOT"

dependencies {
    implementation("net.pascal-lab:tai-e:$taieVersion")
    implementation("org.apache.logging.log4j:log4j-api:2.23.0")
    implementation("org.apache.logging.log4j:log4j-core:2.23.0")

}
