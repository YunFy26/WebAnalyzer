plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.0"  // 版本根据需要调整
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

tasks {
    jar {
        manifest {
            attributes(
                "Main-Class" to "org.example.MyMain",  // 主类路径
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


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")


}
