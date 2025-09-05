package com.adgainai.apolloconfigvisualization.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.TitledSeparator
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel


class ApolloViewConfigurable(private val project: Project) : Configurable {
    private var mainPanel: JPanel? = null
    private var tableModel: DefaultTableModel? = null
    private var keyValueTable: JTable? = null
    private var cookieTextField: JTextField? = null
    private var foldingWhenEveryOpenFileRadioButton: JCheckBox? = null
    private var methodSignatureTextField: JTextArea? = null

    // 新增登录网址、账号和密码输入框变量
    private var accountTextField: JTextField? = null
    private var passwordField: JPasswordField? = null

    public fun createTitledSeparator(title: String): JComponent {
        val panel = JPanel(BorderLayout())
        panel.alignmentY = Component.CENTER_ALIGNMENT


        val label: JLabel = JLabel(title)
        val separator = JSeparator(JSeparator.HORIZONTAL)

        // 设置分割线的尺寸
        separator.preferredSize = Dimension(separator.preferredSize.width, 1)

        // 创建一个垂直布局的盒子
        val verticalBox = Box.createVerticalBox()
        verticalBox.add(Box.createVerticalStrut(10))
        verticalBox.add(separator)
        verticalBox.add(Box.createVerticalStrut(10))


        // 将标签和盒子添加到面板中
        panel.add(label, BorderLayout.WEST)
        panel.add(verticalBox, BorderLayout.CENTER)

        return panel
    }

    override fun getDisplayName(): String {
        return "My Plugin Configuration"
    }


    override fun createComponent(): JComponent? {
        // 初始化输入组件
        foldingWhenEveryOpenFileRadioButton = JCheckBox("是否每次重新打开文件都折叠所有可以折叠的代码").apply {
            border = BorderFactory.createEmptyBorder(0, 100, 0, 0) // 左边缩进20px
        }

        accountTextField = JTextField().apply {
            preferredSize = Dimension(400, 30)
            maximumSize = Dimension(Short.MAX_VALUE.toInt(), 30)
            toolTipText = "Enter your account here"
            border = BorderFactory.createEmptyBorder(0, 100, 0, 0) // 向右100px
        }

        passwordField = JPasswordField().apply {
            preferredSize = Dimension(400, 30)
            maximumSize = Dimension(Short.MAX_VALUE.toInt(), 30)
            toolTipText = "Enter your password here"
            border = BorderFactory.createEmptyBorder(0, 100, 0, 0) // 向右100px
        }

        cookieTextField = JTextField().apply {
            preferredSize = Dimension(400, 30)
            maximumSize = Dimension(Short.MAX_VALUE.toInt(), 30)
            toolTipText = "Enter your cookie here"
            border = BorderFactory.createEmptyBorder(0, 100, 0, 0) // 向右100px
        }

        // 表格
        tableModel = DefaultTableModel(arrayOf("env", "curl"), 0)
        keyValueTable = JTable(tableModel).apply {
            setDefaultRenderer(Any::class.java, CustomTableCellRenderer())
            tableHeader.background = JBColor.LIGHT_GRAY
            rowHeight = 25
            fillsViewportHeight = true
        }

        // 限制表格最大高度 200px
        val scrollPane = JScrollPane(keyValueTable).apply {
            preferredSize = Dimension(400, 120)
            maximumSize = Dimension(Short.MAX_VALUE.toInt(), 200)
            border = BorderFactory.createEmptyBorder(0, 100, 0, 0)
        }

        val addButton = JButton("+").apply {
            preferredSize = Dimension(30, 30)
            addActionListener { tableModel!!.addRow(arrayOf("", "")) }
        }
        val removeButton = JButton("-").apply {
            preferredSize = Dimension(30, 30)
            addActionListener {
                val selectedRow = keyValueTable!!.selectedRow
                if (selectedRow != -1) tableModel!!.removeRow(selectedRow)
            }
        }
        val buttonPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(Box.createRigidArea(Dimension(20, 0))) // 左边缩进20px
            add(addButton)
            add(Box.createRigidArea(Dimension(10, 0)))
            add(removeButton)
            border = BorderFactory.createEmptyBorder(0, 100, 0, 0)
        }

        // MethodSignature 输入框
        methodSignatureTextField = JTextArea().apply {
            lineWrap = true
            wrapStyleWord = true
            preferredSize = Dimension(400, 100)
            maximumSize = Dimension(Short.MAX_VALUE.toInt(), 100)
        }
        val methodSignatureScroll = JScrollPane(methodSignatureTextField!!).apply {
            border = BorderFactory.createEmptyBorder(0, 100, 0, 0)
        }

        // 创建账号/密码/Cookie 的统一 Panel
        val accountPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(0, 100, 0, 0) // 整体向右100px
            add(FormBuilder.createFormBuilder().addLabeledComponent("Account:", accountTextField!!).panel)
            add(Box.createVerticalStrut(5))
            add(FormBuilder.createFormBuilder().addLabeledComponent("Password:", passwordField!!).panel)
            add(Box.createVerticalStrut(5))
            add(FormBuilder.createFormBuilder().addLabeledComponent("Cookie:", cookieTextField!!).panel)
        }

        // 用 FormBuilder 构建主界面
        val formBuilder = FormBuilder.createFormBuilder()
            .setAlignLabelOnRight(true)
            .setHorizontalGap(20)

            // 折叠设置
            .addComponent(TitledSeparator("折叠设置"))
            .addComponent(foldingWhenEveryOpenFileRadioButton!!)

            // 登录方式 / Cookie
            .addComponent(TitledSeparator("获取 cookie：输入账号密码 或者 直接填入 cookie，二选一"))
            .addComponent(accountPanel)

            // 环境配置
            .addComponent(TitledSeparator("获取哪些环境的配置，以及对应环境的 url"))
            .addComponent(buttonPanel)
            .addComponentFillVertically(scrollPane, 5)

            // 方法签名配置
            .addComponent(TitledSeparator("需要折叠的方法签名 (例如: configUtils.getInteger)，逗号分割"))
            .addComponent(methodSignatureScroll)

        // 给整体面板加 padding
        mainPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(0, 40, 0, 0) // 整体左缩进40px
            add(formBuilder.panel, BorderLayout.CENTER)
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
        val isAccountModified = settings.account != (accountTextField?.text ?: "")
        val isPasswordModified = settings.password != String(passwordField?.password ?: CharArray(0))

        return isKeyValuesModified || isCookieModified || isFoldingModified || isMethodSignatureModified || isAccountModified || isPasswordModified
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
        configuration.account = accountTextField?.text ?: ""
        configuration.password = String(passwordField?.password ?: CharArray(0))
    }

    override fun reset() {
        tableModel!!.rowCount = 0 // 清空表格
        val configuration = ApolloViewConfiguration.getInstance(project)
        configuration.keyValues.forEach { keyValue ->
            tableModel!!.addRow(arrayOf<Any?>(keyValue.key, keyValue.value))
        }

        cookieTextField?.text = configuration.cookie
        foldingWhenEveryOpenFileRadioButton!!.isSelected = configuration.foldingWhenEveryOpenFile
        methodSignatureTextField?.text = configuration.methodSignatures
        accountTextField?.text = configuration.account
        passwordField?.text = configuration.password
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
