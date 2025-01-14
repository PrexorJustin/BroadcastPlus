import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
}

val baseVersion = "0.0.1"
val commitHash = System.getenv("COMMIT_HASH")?.takeUnless { it.isBlank() }
val isMainBranch = System.getenv("GITHUB_REF") == "refs/heads/main"
val calculatedVersion = when {
    isMainBranch -> baseVersion
    commitHash != null -> "$baseVersion-dev.$commitHash"
    else -> "$baseVersion-SNAPSHOT"
}

allprojects {
    group = "app.simplecloud.plugin"
    version = calculatedVersion

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.gradleup.shadow")

    dependencies {
        implementation(rootProject.libs.kotlin.jvm)
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    configurations {
        all {
            resolutionStrategy {
                failOnVersionConflict()
                preferProjectModules()
            }
        }
    }

    kotlin {
        jvmToolchain(21)
    }

    tasks {
        withType<KotlinCompile> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }

        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        named("shadowJar", ShadowJar::class) {
            mergeServiceFiles()
            archiveFileName.set("${project.name}.jar")
        }

        processResources {
            expand(
                "version" to project.version,
                "name" to project.name
            )
        }
    }
}

tasks.register("printVersion") {
    doLast {
        println(project.version)
    }
}

tasks.processResources {
    expand(
        "version" to project.version,
        "name" to project.name
    )
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
