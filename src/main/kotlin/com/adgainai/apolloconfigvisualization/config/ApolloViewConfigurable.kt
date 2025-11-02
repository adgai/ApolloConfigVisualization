package com.adgainai.apolloconfigvisualization.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.TitledSeparator
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer

class ApolloViewConfigurable(private val project: Project) : Configurable {
    private var mainPanel: JPanel? = null
    private var projectName: JTextField? = null

    override fun getDisplayName(): String {
        return "Apollo View Configuration"
    }

    override fun createComponent(): JComponent? {
        // ✅ 初始化文本框（否则 projectName!! 会 NPE）
        projectName = JTextField()

        // 创建说明面板
        val projectNamePanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(0, 100, 0, 0)
            add(FormBuilder.createFormBuilder().addLabeledComponent("Project Name:", projectName!!).panel)
            add(Box.createVerticalStrut(5))
        }

        val formBuilder = FormBuilder.createFormBuilder()
            .setAlignLabelOnRight(true)
            .setHorizontalGap(20)
            .addComponent(TitledSeparator("如果 Apollo 中的项目名称与文件夹名不一致，请输入"))
            .addComponent(projectNamePanel)

        mainPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(10, 40, 10, 40)
            add(formBuilder.panel, BorderLayout.CENTER)
        }

        return mainPanel
    }

    override fun isModified(): Boolean {
        val settings = ApolloViewConfiguration.getInstance(project)
        return settings.projectName != (projectName?.text ?: "")
    }

    override fun apply() {
        val settings = ApolloViewConfiguration.getInstance(project)
        settings.projectName = projectName?.text ?: ""
    }

    override fun reset() {
        val settings = ApolloViewConfiguration.getInstance(project)
        projectName?.text = settings.projectName
    }
}

/**
 * 居中显示表格单元格内容
 */
class CustomTableCellRenderer : DefaultTableCellRenderer() {
    override fun getTableCellRendererComponent(
        table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
    ): Component {
        val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        if (component is JLabel) {
            component.horizontalAlignment = JLabel.CENTER
        }
        return component
    }
}
