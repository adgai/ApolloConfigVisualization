package com.adgainai.apolloconfigvisualization.config

import com.intellij.ui.JBColor
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import java.awt.*

//class CustomTableCellRenderer : DefaultTableCellRenderer() {
//    override fun getTableCellRendererComponent(
//        table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
//    ): Component {
//        val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
//        if (row % 2 == 0) {
//            component.background = JBColor.WHITE
//        } else {
//            component.background = Color(224, 255, 224) // Light green
//        }
//        if (isSelected) {
//            component.background = JBColor.GRAY
//        }
//
//        // Set border
//        if (component is JComponent) {
//            component.border = BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.GRAY)
//        }
//
//        return component
//    }
//}
