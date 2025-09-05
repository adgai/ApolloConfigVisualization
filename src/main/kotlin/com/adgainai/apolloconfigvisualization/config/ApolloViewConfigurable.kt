package com.adgainai.apolloconfigvisualization.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.TitledBorder
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
        val panel = JPanel( BorderLayout())
        panel.alignmentY= Component.CENTER_ALIGNMENT


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
        mainPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
        }

        foldingWhenEveryOpenFileRadioButton = JCheckBox("是否每次重新打开文件都折叠所有可以折叠的代码").apply {
            alignmentX = Component.LEFT_ALIGNMENT
        }

        // 定义账号输入框
        accountTextField = JTextField().apply {
            preferredSize = Dimension(400, 30)
            toolTipText = "Enter your account here"
            alignmentX = Component.LEFT_ALIGNMENT
        }

        // 定义密码输入框
        passwordField = JPasswordField().apply {
            preferredSize = Dimension(400, 30)
            toolTipText = "Enter your password here"
            alignmentX = Component.LEFT_ALIGNMENT
        }

        // 定义Cookie输入框
        cookieTextField = JTextField().apply {
            preferredSize = Dimension(400, 30)
            toolTipText = "Enter your cookie here"
            alignmentX = Component.LEFT_ALIGNMENT
        }

        // 定义带有两列的表模型：Key和Value
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

        // 创建按钮面板并设置其布局
        val buttonPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            add(addButton)
            add(Box.createRigidArea(Dimension(10, 0)))  // 添加按钮之间的空白
            add(removeButton)
        }

        val foldingPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            add(foldingWhenEveryOpenFileRadioButton)
        }


        // 创建一个 JFram("")
        val foldingSetting = createTitledSeparator("折叠设置")
        val getCookieSetting = createTitledSeparator("获取cookie，输入账号密码或者直接填入cookie,两种方式二选一")
        val getEnv = createTitledSeparator("获取哪些环境的配置，以及对应环境的url")
        val foldingMethodSignatureJSeparator = createTitledSeparator("需要折叠的方法的签名类似：configUtils.getInteger,逗号分割")

        // 将组件添加到主面板
        mainPanel!!.apply {

            add(foldingSetting)
            add(foldingPanel)

            add(getCookieSetting)

            // 添加账号和密码输入框
            val accountPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JBLabel("Account: "), accountTextField!!, 4, false)
                .panel.also {
                    it.alignmentX = Component.LEFT_ALIGNMENT
                }
            add(accountPanel)

            val passwordPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JBLabel("Password: "), passwordField!!, 4, false)
                .panel.also {
                    it.alignmentX = Component.LEFT_ALIGNMENT
                }
            add(passwordPanel)

            val cookiePanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JBLabel("Cookie: "), cookieTextField!!, 4, false)
                .panel.also {
                    it.alignmentX = Component.LEFT_ALIGNMENT
                }
            add(cookiePanel)

            add(getEnv)
            add(buttonPanel)
            add(scrollPane)

            // 添加MethodSignature输入框
            methodSignatureTextField = JTextArea().apply {
                lineWrap = true
                wrapStyleWord = true
                preferredSize = Dimension(400, 100)
            }

            add(foldingMethodSignatureJSeparator)
            val methodSignaturePanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JBLabel(""), JScrollPane(methodSignatureTextField!!), 4, false)
                .panel.also {
                    it.alignmentX = Component.LEFT_ALIGNMENT
                }
            add(methodSignaturePanel)
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
