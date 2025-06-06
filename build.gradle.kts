plugins {
    java
    id ("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "top.mrxiaom"
version = "1.0.8"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    // https://github.com/mcio-dev/MinecraftAuthHeadless
    implementation("com.github.mcio-dev:MinecraftAuthHeadless:v4.1.2-patch1")
    implementation("com.github.technicallycoded:FoliaLib:0.4.4")
    implementation("org.jetbrains:annotations:24.0.0")

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
            "com.google.gson" to "gson", // minecraft auth
            "io.jsonwebtoken" to "jsonwebtoken", // minecraft auth
            "com.google.errorprone.annotations" to "annotations.google",
            "net.lenni0451.commons.httpclient" to "httpclient",
            "com.tcoded.folialib" to "folialib",
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
