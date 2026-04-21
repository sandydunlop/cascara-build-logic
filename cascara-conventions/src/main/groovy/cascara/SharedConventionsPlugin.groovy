package cascara

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.testing.jacoco.tasks.JacocoReport

class SharedConventionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.pluginManager.apply 'jacoco'
        project.pluginManager.apply 'signing'
        project.pluginManager.apply 'maven-publish'

        // --- Java configuration ---
        project.extensions.configure(org.gradle.api.plugins.JavaPluginExtension) { java ->
            java.withJavadocJar()
            java.withSourcesJar()
            java.toolchain.languageVersion = JavaLanguageVersion.of(25)
        }

        // --- runtimeModulePath task ---
        project.tasks.register('runtimeModulePath', Copy) { t ->
            t.dependsOn project.tasks.named('jar')
            t.from project.configurations.runtimeClasspath
            t.duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
            t.into project.layout.buildDirectory.dir('modulepath')
        }

        project.tasks.named('build') {
            it.finalizedBy 'runtimeModulePath'
        }

        // --- Test configuration ---
        project.tasks.withType(org.gradle.api.tasks.testing.Test).configureEach { test ->
            test.useJUnitPlatform()
            test.testLogging {
                events TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED
            }
            test.finalizedBy 'jacocoTestReport'
        }

        // --- Jacoco ---
        project.extensions.configure(org.gradle.testing.jacoco.plugins.JacocoPluginExtension) {
            it.toolVersion = '0.8.13'
        }

        project.tasks.withType(JacocoReport).configureEach { report ->
            report.dependsOn 'test'
            report.reports {
                xml.required = true
                html.required = true
            }
        }

        // --- Jar manifest ---
        project.tasks.withType(Jar).configureEach { jar ->
            jar.manifest.attributes(
                'Implementation-Title': project.providers.gradleProperty('maven_name'),
                'Implementation-Version': project.version,
                'Implementation-Vendor': project.group,
                'Automatic-Module-Name': project.providers.gradleProperty('java_name'),
                'Build-Date': new Date().toString()
            )
        }
    }
}
