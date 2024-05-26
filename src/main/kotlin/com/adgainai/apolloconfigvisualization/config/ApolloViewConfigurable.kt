package com.adgainai.apolloconfigvisualization.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import org.jetbrains.annotations.Nls
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.table.DefaultTableModel


class ApolloViewConfigurable(private val project: Project) : Configurable {
    private var mainPanel: JPanel? = null
    private var tableModel: DefaultTableModel? = null
    private var keyValueTable: JTable? = null
    private var cookieTextField: JTextField? = null
    private var foldingWhenEveryOpenFileRadioButton: JRadioButton? = null


    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String? {
        return "My Plugin Configuration"
    }

    override fun createComponent(): JComponent? {
        mainPanel = JPanel(GridBagLayout())


        foldingWhenEveryOpenFileRadioButton = JRadioButton("是否每次重新打开文件都折叠所有可以折叠的代码:")

        // Define the cookie input field
        cookieTextField = JTextField().apply {
            preferredSize = Dimension(400, 30)
            toolTipText = "Enter your cookie here"
        }

        // Define table model with two columns: Key and Value
        tableModel = DefaultTableModel(arrayOf<Any>("env", "curl"), 0)

        keyValueTable = JTable(tableModel).apply {
            setDefaultRenderer(Any::class.java, CustomTableCellRenderer())
            tableHeader.background = JBColor.LIGHT_GRAY
            rowHeight = 25
            fillsViewportHeight = true
        }

        keyValueTable!!.fillsViewportHeight = true
        keyValueTable!!.rowHeight = 25

        val scrollPane = JScrollPane(keyValueTable)
        scrollPane.preferredSize = Dimension(400, 200)

        val addButton = JButton("Add")
        addButton.preferredSize = Dimension(80, 30)
        addButton.addActionListener { e: ActionEvent? ->
            tableModel!!.addRow(
                arrayOf<Any>(
                    "",
                    ""
                )
            )
        }

        val removeButton = JButton("Remove")
        removeButton.preferredSize = Dimension(80, 30)
        removeButton.addActionListener { e: ActionEvent? ->
            val selectedRow = keyValueTable!!.selectedRow
            if (selectedRow != -1) {
                tableModel!!.removeRow(selectedRow)
            }
        }


        // Layout constraints
        val gbc = GridBagConstraints().apply {
            insets = JBUI.insets(10)
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.NORTH
        }

        gbc.apply {
            gridx = 0
            gridy = 0
            gridwidth = 2
        }

        mainPanel!!.add(foldingWhenEveryOpenFileRadioButton)

        gbc.gridx = 0
        gbc.gridy = 1
        val panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Cookie: "), cookieTextField!!, 4, false)
            .panel

        mainPanel!!.add(panel, gbc)


        gbc.insets = JBUI.insets(10)
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.anchor = GridBagConstraints.NORTH

        gbc.gridx = 0
        gbc.gridy = 2
        gbc.gridwidth = 2
        mainPanel!!.add(scrollPane, gbc)

        gbc.gridx = 0
        gbc.gridy = 3
        gbc.gridwidth = 1
        mainPanel!!.add(addButton, gbc)

        gbc.gridx = 1
        gbc.gridy = 3
        gbc.gridwidth = 1
        mainPanel!!.add(removeButton, gbc)

        return mainPanel
    }

    override fun isModified(): Boolean {
        val settings = ApolloViewConfiguration.getInstance(
            project
        )
        val storedKeyValues: List<ApolloViewConfiguration.KeyValue> = settings.keyValues
        val b = storedKeyValues != currentKeyValues
        val b1 = !settings.cookie.equals(cookieTextField?.text ?: "")
        val b2 = !settings.foldingWhenEveryOpenFile.equals(foldingWhenEveryOpenFileRadioButton!!.isSelected)

        return b || b1 || b2


    }

    override fun apply() {
        val configuration = ApolloViewConfiguration.getInstance(project)
        configuration.keyValues.clear()
        val currentKeyValues =
            currentKeyValues
        for (keyValue in currentKeyValues) {
            configuration.addKeyValue(keyValue.key, keyValue.value)
        }

        configuration.cookie = cookieTextField?.text ?: ""
        configuration.foldingWhenEveryOpenFile = foldingWhenEveryOpenFileRadioButton!!.isSelected

    }

    override fun reset() {
        tableModel!!.rowCount = 0 // Clear the table
        val configuration = ApolloViewConfiguration.getInstance(
            project
        )
        val keyValues: List<ApolloViewConfiguration.KeyValue> = configuration.keyValues
        for (keyValue in keyValues) {
            tableModel!!.addRow(arrayOf<Any?>(keyValue.key, keyValue.value))
        }

        cookieTextField?.text = configuration.cookie
        foldingWhenEveryOpenFileRadioButton!!.isSelected = configuration.foldingWhenEveryOpenFile
    }

    private val currentKeyValues: List<ApolloViewConfiguration.KeyValue>
        get() {
            val keyValues: MutableList<ApolloViewConfiguration.KeyValue> = ArrayList()
            val rowCount = tableModel!!.rowCount
            for (i in 0 until rowCount) {
                val key = tableModel!!.getValueAt(i, 0) as String
                val value = tableModel!!.getValueAt(i, 1) as String
                val keyValue = ApolloViewConfiguration.KeyValue()
                keyValue.key = key
                keyValue.value = value
                keyValues.add(keyValue)
            }
            return keyValues
        }
}

