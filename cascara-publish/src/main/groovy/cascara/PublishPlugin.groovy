package cascara

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class PublishPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.pluginManager.apply("maven-publish")
        project.pluginManager.apply("signing")

        project.afterEvaluate {
            if (project.plugins.hasPlugin("maven-publish")) {

                project.publishing {
                    publications { pubs ->

                        // Gradle plugin projects
                        if (project.plugins.hasPlugin("java-gradle-plugin")) {
                            pubs.withType(MavenPublication).configureEach { pub ->
                                configurePom(pub, project)
                            }
                        }

                        // Java libraries
                        if (project.plugins.hasPlugin("java-library")) {
                            pubs.create("mavenJava", MavenPublication) { pub ->
                                pub.from(project.components.getByName("java"))
                                configurePom(pub, project)
                            }
                        }

                        // BOMs
                        if (project.plugins.hasPlugin("java-platform")) {
                            pubs.create("mavenBom", MavenPublication) { pub ->
                                pub.from(project.components.getByName("javaPlatform"))
                                configurePom(pub, project)
                            }
                        }
                    }

                    repositories {
                        maven {
                            name = "MavenCentral"
                            url = project.version.toString().endsWith("SNAPSHOT") ?
                                    project.uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") :
                                    project.uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                            credentials {
                                username = project.findProperty("ossrhUsername")
                                password = project.findProperty("ossrhPassword")
                            }
                        }
                    }
                }

                project.signing {
                    sign project.publishing.publications
                }
            }
        }
    }

    private static void configurePom(MavenPublication pub, Project project) {
        pub.pom {
            name = project.name
            description = "Cascara module: ${project.name}"
            url = "https://github.com/qishr/cascara"

            licenses {
                license {
                    name = "Apache License 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0"
                }
            }

            scm {
                url = "https://github.com/qishr/cascara"
                connection = "scm:git:https://github.com/qishr/cascara.git"
                developerConnection = "scm:git:https://github.com/qishr/cascara.git"
            }

            developers {
                developer {
                    id = "sandy"
                    name = "Sandy"
                }
            }
        }
    }
}
