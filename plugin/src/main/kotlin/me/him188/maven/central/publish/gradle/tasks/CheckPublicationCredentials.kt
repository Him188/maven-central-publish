package me.him188.maven.central.publish.gradle.tasks

import me.him188.maven.central.publish.gradle.mcExt
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CheckPublicationCredentials : DefaultTask() {
    companion object {
        const val TASK_NAME = "checkPublicationCredentials"
    }

    @TaskAction
    fun run() {
        val ext = project.mcExt
        check(ext.projectUrl.isNotBlank()) { "'projectUrl' is not set. This means `mavenCentralPublish` is not configured." }
        check(ext.connection.isNotBlank()) { "'connection' is not set. This means `mavenCentralPublish` is not configured." }
    }
}