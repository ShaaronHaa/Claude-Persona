package com.claude.persona.service

import com.claude.persona.model.Persona
import com.claude.persona.settings.PersonaSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.RunResult
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.YearMonth
import kotlin.random.Random

class PersonaService {

    private val personas: List<Persona> by lazy {
        loadPersonas()
    }

    private fun loadPersonas(): List<Persona> {
        val stream = javaClass.classLoader.getResourceAsStream("personas.json")
            ?: return emptyList()
        val reader = InputStreamReader(stream)
        val type = object : TypeToken<List<Persona>>() {}.type
        return Gson().fromJson(reader, type)
    }

    fun getAllPersonas(): List<Persona> = personas

    fun getPersonaById(id: String): Persona? = personas.find { it.id == id }

    fun getRandomPersona(): Persona {
        return personas[Random.nextInt(personas.size)]
    }

    fun getPersonaByDate(date: LocalDate): Persona {
        val settings = PersonaSettings.getInstance()
        val seed = when (settings.switchMode) {
            PersonaSettings.SwitchMode.DAILY -> date.dayOfYear.toLong()
            PersonaSettings.SwitchMode.MONTHLY -> YearMonth.from(date).atDay(1).dayOfYear.toLong()
            else -> Random.nextLong()
        }
        val random = Random(seed)
        return personas[random.nextInt(personas.size)]
    }

    fun getCurrentPersona(): Persona? {
        val settings = PersonaSettings.getInstance()

        return when (settings.switchMode) {
            PersonaSettings.SwitchMode.MANUAL -> {
                settings.currentPersonaId?.let { getPersonaById(it) }
            }
            PersonaSettings.SwitchMode.DAILY -> {
                val today = LocalDate.now().toString()
                if (settings.lastSwitchDate != today) {
                    settings.lastSwitchDate = today
                    val persona = getPersonaByDate(LocalDate.now())
                    settings.currentPersonaId = persona.id
                    persona
                } else {
                    settings.currentPersonaId?.let { getPersonaById(it) }
                        ?: getPersonaByDate(LocalDate.now())
                }
            }
            PersonaSettings.SwitchMode.MONTHLY -> {
                val thisMonth = YearMonth.now().toString()
                if (settings.lastSwitchDate != thisMonth) {
                    settings.lastSwitchDate = thisMonth
                    val persona = getPersonaByDate(LocalDate.now())
                    settings.currentPersonaId = persona.id
                    persona
                } else {
                    settings.currentPersonaId?.let { getPersonaById(it) }
                        ?: getPersonaByDate(LocalDate.now())
                }
            }
        }
    }

    fun setCurrentPersona(persona: Persona) {
        val settings = PersonaSettings.getInstance()
        settings.currentPersonaId = persona.id
        settings.lastSwitchDate = LocalDate.now().toString()
    }

    fun applyPersonaToProject(project: Project, persona: Persona): Boolean {
        val basePath = project.basePath ?: return false
        val claudeMdPath = "$basePath/CLAUDE.md"

        val fileSystem = LocalFileSystem.getInstance()
        val claudeMdFile = fileSystem.findFileByPath(claudeMdPath)

        val existingContent = if (claudeMdFile != null && claudeMdFile.exists()) {
            String(claudeMdFile.contentsToByteArray())
        } else {
            ""
        }

        val cleanedContent = removePersonaSection(existingContent)

        val newContent = if (cleanedContent.isNotBlank()) {
            "$cleanedContent\n\n${persona.getPromptSection()}"
        } else {
            persona.getPromptSection()
        }

        return try {
            val application = ApplicationManager.getApplication()

            if (claudeMdFile == null) {
                // 创建新文件
                val baseDir = fileSystem.findFileByPath(basePath) ?: return false

                application.invokeAndWait {
                    WriteCommandAction.runWriteCommandAction(project) {
                        val newFile = baseDir.createChildData(this, "CLAUDE.md")
                        newFile.setBinaryContent(newContent.toByteArray(Charsets.UTF_8))
                    }
                }
            } else {
                // 更新现有文件
                application.invokeAndWait {
                    WriteCommandAction.runWriteCommandAction(project) {
                        claudeMdFile.setBinaryContent(newContent.toByteArray(Charsets.UTF_8))
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun removePersonaFromProject(project: Project): Boolean {
        val basePath = project.basePath ?: return false
        val claudeMdPath = "$basePath/CLAUDE.md"

        val fileSystem = LocalFileSystem.getInstance()
        val claudeMdFile = fileSystem.findFileByPath(claudeMdPath) ?: return true

        val existingContent = String(claudeMdFile.contentsToByteArray())
        val cleanedContent = removePersonaSection(existingContent).trim()

        return try {
            val application = ApplicationManager.getApplication()

            application.invokeAndWait {
                WriteCommandAction.runWriteCommandAction(project) {
                    if (cleanedContent.isBlank()) {
                        claudeMdFile.delete(this)
                    } else {
                        claudeMdFile.setBinaryContent(cleanedContent.toByteArray(Charsets.UTF_8))
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun removePersonaSection(content: String): String {
        val marker = "## 🎭 Claude人设"
        val index = content.indexOf(marker)
        return if (index >= 0) {
            content.substring(0, index).trimEnd()
        } else {
            content
        }
    }

    companion object {
        fun getInstance(): PersonaService {
            return ApplicationManager.getApplication().getService(PersonaService::class.java)
        }
    }
}
