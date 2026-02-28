package com.claude.persona.ui

import com.claude.persona.model.Persona
import com.claude.persona.service.PersonaService
import com.claude.persona.settings.PersonaSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*

class PersonaToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = PersonaToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

class PersonaToolWindowPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val personaService = PersonaService.getInstance()
    private val settings = PersonaSettings.getInstance()

    private val categoryCombo = ComboBox<String>()
    private val personaList = JList<Persona>()
    private val personaListModel = DefaultListModel<Persona>()
    private val stylePreviewArea = JTextArea()
    private val switchModeCombo = ComboBox<PersonaSettings.SwitchMode>()

    init {
        setupUI()
        loadInitialData()
    }

    private fun setupUI() {
        // 顶部控制面板
        val topPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JLabel("分类:"))
            add(categoryCombo.apply {
                preferredSize = Dimension(150, 30)
                addActionListener { filterPersonasByCategory() }
            })
            add(JLabel("切换模式:"))
            add(switchModeCombo.apply {
                addItem(PersonaSettings.SwitchMode.MANUAL)
                addItem(PersonaSettings.SwitchMode.DAILY)
                addItem(PersonaSettings.SwitchMode.MONTHLY)
                selectedItem = settings.switchMode
                preferredSize = Dimension(100, 30)
                addActionListener {
                    settings.switchMode = selectedItem as PersonaSettings.SwitchMode
                }
            })
        }

        // 左侧人设列表
        val leftPanel = JPanel(BorderLayout()).apply {
            add(JBScrollPane(personaList.apply {
                model = personaListModel
                cellRenderer = PersonaListCellRenderer()
                selectionMode = ListSelectionModel.SINGLE_SELECTION
                addListSelectionListener { onPersonaSelected() }
            }), BorderLayout.CENTER)
            preferredSize = Dimension(250, 400)
        }

        // 右侧预览面板
        val rightPanel = JPanel(BorderLayout()).apply {
            add(JLabel("交互风格预览:"), BorderLayout.NORTH)
            add(JBScrollPane(stylePreviewArea.apply {
                isEditable = false
                lineWrap = true
                wrapStyleWord = true
                rows = 15
                columns = 40
            }), BorderLayout.CENTER)
        }

        // 底部按钮面板
        val bottomPanel = JPanel(FlowLayout(FlowLayout.CENTER)).apply {
            add(JButton("🎲 随机选择").apply {
                addActionListener { randomSelect() }
            })
            add(JButton("✅ 应用人设").apply {
                addActionListener { applyPersona() }
            })
            add(JButton("🗑️ 移除人设").apply {
                addActionListener { removePersona() }
            })
        }

        // 布局组装
        val centerPanel = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel).apply {
            resizeWeight = 0.3
        }

        add(topPanel, BorderLayout.NORTH)
        add(centerPanel, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)
    }

    private fun loadInitialData() {
        // 加载分类
        val categories = personaService.getAllPersonas()
            .map { it.category }
            .distinct()
            .sorted()
        categoryCombo.addItem("全部")
        categories.forEach { categoryCombo.addItem(it) }

        // 加载所有人设
        refreshPersonaList(null)

        // 选中当前人设
        personaService.getCurrentPersona()?.let { current ->
            personaListModel.elements().asSequence()
                .forEachIndexed { index, persona ->
                    if (persona.id == current.id) {
                        personaList.selectedIndex = index
                    }
                }
        }
    }

    private fun filterPersonasByCategory() {
        val selectedCategory = categoryCombo.selectedItem as? String
        refreshPersonaList(if (selectedCategory == "全部") null else selectedCategory)
    }

    private fun refreshPersonaList(category: String?) {
        personaListModel.clear()
        personaService.getAllPersonas()
            .filter { category == null || it.category == category }
            .forEach { personaListModel.addElement(it) }
    }

    private fun onPersonaSelected() {
        val selected = personaList.selectedValue ?: return
        stylePreviewArea.text = buildString {
            appendLine("【${selected.cp}】")
            appendLine("作品：${selected.work}")
            appendLine("分类：${selected.category}")
            appendLine()
            appendLine("交互风格：")
            appendLine(selected.style)
            appendLine()
            appendLine("称呼用户：${selected.callName}")
        }
    }

    private fun randomSelect() {
        val randomPersona = personaService.getRandomPersona()
        for (i in 0 until personaListModel.size()) {
            if (personaListModel[i].id == randomPersona.id) {
                personaList.selectedIndex = i
                break
            }
        }
    }

    private fun applyPersona() {
        val selected = personaList.selectedValue
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个人设", "提示", JOptionPane.WARNING_MESSAGE)
            return
        }

        val success = personaService.applyPersonaToProject(project, selected)
        if (success) {
            personaService.setCurrentPersona(selected)
            JOptionPane.showMessageDialog(
                this,
                "已应用人设【${selected.cp}】到 CLAUDE.md\n请重启 Claude Code 使人设生效",
                "成功",
                JOptionPane.INFORMATION_MESSAGE
            )
        } else {
            JOptionPane.showMessageDialog(this, "应用人设失败", "错误", JOptionPane.ERROR_MESSAGE)
        }
    }

    private fun removePersona() {
        val confirm = JOptionPane.showConfirmDialog(
            this,
            "确定要从 CLAUDE.md 中移除人设吗？",
            "确认",
            JOptionPane.YES_NO_OPTION
        )
        if (confirm == JOptionPane.YES_OPTION) {
            val success = personaService.removePersonaFromProject(project)
            if (success) {
                settings.currentPersonaId = null
                JOptionPane.showMessageDialog(this, "已移除人设", "成功", JOptionPane.INFORMATION_MESSAGE)
            } else {
                JOptionPane.showMessageDialog(this, "移除人设失败", "错误", JOptionPane.ERROR_MESSAGE)
            }
        }
    }
}

class PersonaListCellRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): JLabel {
        val persona = value as? Persona
        val displayText = persona?.let { "${it.cp} (${it.work})" } ?: ""
        super.getListCellRendererComponent(list, displayText, index, isSelected, cellHasFocus)
        return this
    }
}