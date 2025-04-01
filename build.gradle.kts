plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.0"
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
    mainClass.set("org.example.Main")
}

val taieVersion = "0.5.2-SNAPSHOT" // or the latest version "0.5.2-SNAPSHOT"

tasks {
    jar {
        manifest {
            attributes(
                "Main-Class" to "org.example.Main",  // 主类路径
                "Tai-e-Version" to taieVersion,
            )
        }
    }
}
dependencies {
    implementation("net.pascal-lab:tai-e:$taieVersion")
    implementation("org.apache.logging.log4j:log4j-api:2.23.0")
    implementation("org.apache.logging.log4j:log4j-core:2.23.0")
    implementation("commons-cli:commons-cli:1.4")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
    implementation("com.knuddels:jtokkit:1.1.0")





    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")


}
