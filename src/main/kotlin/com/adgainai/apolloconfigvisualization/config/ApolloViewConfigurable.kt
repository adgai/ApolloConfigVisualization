package com.adgainai.apolloconfigvisualization.config

import com.intellij.find.impl.JComboboxAction.Companion.emptyText
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import org.jetbrains.annotations.Nls
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel

class ApolloViewConfigurable(private val project: Project) : Configurable {
    private var mainPanel: JPanel? = null
    private var tableModel: DefaultTableModel? = null
    private var keyValueTable: JTable? = null
    private var cookieTextField: JTextField? = null
    private var foldingWhenEveryOpenFileRadioButton: JCheckBox? = null
    private var methodSignatureTextField: JTextField? = null

    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String? {
        return "My Plugin Configuration"
    }

    override fun createComponent(): JComponent? {
        mainPanel = JPanel()
            .apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                alignmentX = Component.LEFT_ALIGNMENT
            }

        foldingWhenEveryOpenFileRadioButton = JCheckBox("是否每次重新打开文件都折叠所有可以折叠的代码")
            .apply {
                alignmentX = Component.LEFT_ALIGNMENT
            }

        // Define the cookie input field
        cookieTextField = JTextField().apply {
            preferredSize = Dimension(400, 30)
            toolTipText = "Enter your cookie here"
            alignmentX = Component.LEFT_ALIGNMENT
        }

        // Define table model with two columns: Key and Value
        tableModel = DefaultTableModel(arrayOf("env", "curl"), 0)

        keyValueTable = JTable(tableModel).apply {
            setDefaultRenderer(Any::class.java, CustomTableCellRenderer())
            tableHeader.background = JBColor.LIGHT_GRAY
            rowHeight = 25
            fillsViewportHeight = true
            alignmentX = Component.LEFT_ALIGNMENT
        }

        val scrollPane = JScrollPane(keyValueTable).apply {
            preferredSize = Dimension(400, 200)
            alignmentX = Component.LEFT_ALIGNMENT
        }

        methodSignatureTextField = JTextField()
        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("MethodSignatureField: "), methodSignatureTextField!!, 4, false)
            .panel

        val addButton = JButton("+").apply {
            preferredSize = Dimension(30, 30)
            alignmentX = Component.LEFT_ALIGNMENT
            addActionListener {
                tableModel!!.addRow(arrayOf("", ""))
            }
        }

        val removeButton = JButton("-").apply {
            preferredSize = Dimension(30, 30)
            alignmentX = Component.LEFT_ALIGNMENT
            addActionListener {
                val selectedRow = keyValueTable!!.selectedRow
                if (selectedRow != -1) {
                    tableModel!!.removeRow(selectedRow)
                }
            }
        }

        // Create a panel for the buttons and set its layout
        val buttonPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            add(addButton)
            add(Box.createRigidArea(Dimension(10, 0)))  // Add some space between buttons
            add(removeButton)
        }

        val foldingPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            add(foldingWhenEveryOpenFileRadioButton)
        }

        // Add components to the main panel
        mainPanel!!.apply {
            add(foldingPanel)
            add(panel)
//            add(Box.createRigidArea(Dimension(0, 20)))  // Add vertical space of 30
            val panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JBLabel("Cookie: "), cookieTextField!!, 4, false)
                .panel.also {
                    it.alignmentX = Component.LEFT_ALIGNMENT
                }
            add(panel)
//            add(Box.createRigidArea(Dimension(0, 20)))  // Add vertical space of 30
            add(buttonPanel)
//            add(Box.createRigidArea(Dimension(0, 20)))  // Add vertical space of 30
            add(scrollPane)
        }

        return mainPanel
    }

    override fun isModified(): Boolean {
        val settings = ApolloViewConfiguration.getInstance(project)
        val storedKeyValues = settings.keyValues
        val isKeyValuesModified = storedKeyValues != currentKeyValues
        val isCookieModified = settings.cookie != (cookieTextField?.text ?: "")
        val isFoldingModified = settings.foldingWhenEveryOpenFile != foldingWhenEveryOpenFileRadioButton!!.isSelected
        val isMethodSignatureModified = settings.methodSignatures != methodSignatureTextField?.text

        return isKeyValuesModified || isCookieModified || isFoldingModified || isMethodSignatureModified
    }

    override fun apply() {
        val configuration = ApolloViewConfiguration.getInstance(project)
        configuration.keyValues.clear()
        currentKeyValues.forEach { keyValue ->
            configuration.addKeyValue(keyValue.key, keyValue.value)
        }

        configuration.cookie = cookieTextField?.text ?: ""
        configuration.foldingWhenEveryOpenFile = foldingWhenEveryOpenFileRadioButton!!.isSelected
        configuration.methodSignatures = methodSignatureTextField?.text ?: ""

    }

    override fun reset() {
        tableModel!!.rowCount = 0 // Clear the table
        val configuration = ApolloViewConfiguration.getInstance(project)
        configuration.keyValues.forEach { keyValue ->
            tableModel!!.addRow(arrayOf<Any?>(keyValue.key, keyValue.value))
        }

        cookieTextField?.text = configuration.cookie
        foldingWhenEveryOpenFileRadioButton!!.isSelected = configuration.foldingWhenEveryOpenFile
        methodSignatureTextField?.text = configuration.methodSignatures
    }

    private val currentKeyValues: List<ApolloViewConfiguration.KeyValue>
        get() {
            val keyValues = mutableListOf<ApolloViewConfiguration.KeyValue>()
            val rowCount = tableModel!!.rowCount
            for (i in 0 until rowCount) {
                val key = tableModel!!.getValueAt(i, 0) as String
                val value = tableModel!!.getValueAt(i, 1) as String
                val element = ApolloViewConfiguration.KeyValue()
                element.key = key
                element.value = value
                keyValues.add(element)
            }
            return keyValues
        }
}

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
