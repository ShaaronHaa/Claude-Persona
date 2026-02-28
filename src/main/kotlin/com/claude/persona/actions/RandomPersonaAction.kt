package com.claude.persona.actions

import com.claude.persona.service.PersonaService
import com.claude.persona.settings.PersonaSettings
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages

class RandomPersonaAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val personaService = PersonaService.getInstance()

        val randomPersona = personaService.getRandomPersona()

        val result = Messages.showYesNoDialog(
            project,
            "随机选中人设：【${randomPersona.cp}】\n来自作品：${randomPersona.work}\n\n是否应用到当前项目？",
            "随机人设",
            "应用",
            "取消",
            Messages.getQuestionIcon()
        )

        if (result == Messages.YES) {
            val success = personaService.applyPersonaToProject(project, randomPersona)
            if (success) {
                personaService.setCurrentPersona(randomPersona)
                showNotification(
                    project,
                    "已应用人设【${randomPersona.cp}】",
                    "请重启 Claude Code 使人设生效",
                    NotificationType.INFORMATION
                )
            } else {
                showNotification(
                    project,
                    "应用失败",
                    "无法写入 CLAUDE.md 文件",
                    NotificationType.ERROR
                )
            }
        }
    }

    private fun showNotification(project: com.intellij.openapi.project.Project, title: String, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Claude Persona")
            .createNotification(title, content, type)
            .notify(project)
    }
}