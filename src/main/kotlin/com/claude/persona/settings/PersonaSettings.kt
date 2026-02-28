package com.claude.persona.settings

import com.claude.persona.model.Persona
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "PersonaSettings",
    storages = [Storage("ClaudePersonaSettings.xml")]
)
class PersonaSettings : PersistentStateComponent<PersonaSettings> {

    var currentPersonaId: String? = null
    var switchMode: SwitchMode = SwitchMode.MANUAL
    var lastSwitchDate: String = ""

    enum class SwitchMode {
        MANUAL,      // 手动切换
        DAILY,       // 每日随机
        MONTHLY      // 每月随机
    }

    override fun getState(): PersonaSettings = this

    override fun loadState(state: PersonaSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): PersonaSettings {
            return ApplicationManager.getApplication().getService(PersonaSettings::class.java)
        }
    }
}