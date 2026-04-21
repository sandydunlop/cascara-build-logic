package cascara

import org.gradle.api.Plugin
import org.gradle.api.Project

class JavaLibraryConventionsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply("java-library")
        project.pluginManager.apply("cascara.shared-conventions")
    }
}
