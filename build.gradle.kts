plugins {
    java
    id ("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "top.mrxiaom"
version = "1.0.2"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.5")

    implementation("net.raphimc:MinecraftAuth:4.1.0")
    implementation("org.jetbrains:annotations:21.0.0")

    testImplementation("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}
val targetJavaVersion = 8
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}
tasks {
    withType<Jar>().configureEach {
        from("LICENSE")
        from("src/main/generated/resources/config.yml")
    }
    shadowJar {
        archiveClassifier.set("")
        mapOf(
            "org.intellij.lang.annotations" to "annotations.intellij",
            "org.jetbrains.annotations" to "annotations.jetbrains",
            "net.raphimc.minecraftauth" to "auth",
            "com.google.errorprone.annotations" to "annotations.google",
            "com.google.gson" to "gson",
            "io.jsonwebtoken" to "jsonwebtoken",
            "net.lenni0451.commons.httpclient" to "httpclient",
        ).forEach { (original, target) ->
            relocate(original, "top.mrxiaom.premiumverify.utils.$target")
        }
    }
    build {
        dependsOn(shadowJar)
    }
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
    test {
        useJUnitPlatform()
        onlyIf { true }
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf("version" to version))
            include("plugin.yml")
        }
        exclude("config.yml")
        finalizedBy(test)
    }
}
