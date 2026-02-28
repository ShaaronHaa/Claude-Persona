package com.claude.persona.settings

import com.claude.persona.service.PersonaService
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

class PersonaSettingsConfigurable : Configurable {

    private val settings = PersonaSettings.getInstance()
    private val switchModeCombo = ComboBox<PersonaSettings.SwitchMode>()

    override fun getDisplayName(): String = "Claude Persona"

    override fun createComponent(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            insets = Insets(5, 5, 5, 5)
            anchor = GridBagConstraints.WEST
        }

        gbc.gridx = 0
        gbc.gridy = 0
        panel.add(JLabel("默认切换模式:"), gbc)

        gbc.gridx = 1
        switchModeCombo.addItem(PersonaSettings.SwitchMode.MANUAL)
        switchModeCombo.addItem(PersonaSettings.SwitchMode.DAILY)
        switchModeCombo.addItem(PersonaSettings.SwitchMode.MONTHLY)
        switchModeCombo.selectedItem = settings.switchMode
        panel.add(switchModeCombo, gbc)

        gbc.gridx = 0
        gbc.gridy = 1
        gbc.gridwidth = 2
        panel.add(JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JLabel("<html><i>MANUAL: 手动切换人设<br>DAILY: 每天自动随机<br>MONTHLY: 每月自动随机</i></html>"))
        }, gbc)

        return panel
    }

    override fun isModified(): Boolean {
        return switchModeCombo.selectedItem != settings.switchMode
    }

    override fun apply() {
        settings.switchMode = switchModeCombo.selectedItem as PersonaSettings.SwitchMode
    }

    override fun reset() {
        switchModeCombo.selectedItem = settings.switchMode
    }
}