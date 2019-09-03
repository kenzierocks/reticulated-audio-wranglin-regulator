import org.gradle.plugins.ide.idea.model.IdeaModel

apply(plugin = "idea")
configure<IdeaModel> {
    project.jdkName = "12"
}
