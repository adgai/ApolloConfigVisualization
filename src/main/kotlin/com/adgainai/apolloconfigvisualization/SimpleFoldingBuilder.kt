// Copyright 2000-2023 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.adgainai.apolloconfigvisualization

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.util.PsiLiteralUtil
import com.intellij.util.containers.toArray



class SimpleFoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        // Initialize the group of folding regions that will expand/collapse together.
        val group = FoldingGroup.newGroup("apolloconfig")
        // Initialize the list of folding regions
        val descriptors: MutableList<FoldingDescriptor> = ArrayList()


        root.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitLiteralExpression(literalExpression: PsiLiteralExpression) {
                super.visitLiteralExpression(literalExpression)

                val value = PsiLiteralUtil.getStringLiteralContent(literalExpression)
                if (value != null && value.startsWith("simple:")) {
                    val project = literalExpression.project
                    val key: String = value
                    // find SimpleProperty for the given key in the project
//          SimpleProperty simpleProperty = ContainerUtil.getOnlyItem(SimpleUtil.findProperties(project, key));
//                    if (true) {
                    // Add a folding descriptor for the literal expression at this node.
                    descriptors.add(
                        FoldingDescriptor(
                            literalExpression.node,
                            TextRange(
                                literalExpression.textRange.startOffset + 1,
                                literalExpression.textRange.endOffset - 1
                            ),
                            null,
                            "xxxx",
                            true,
                            setOf(document)
                        )
                    )
//                    }
                }
            }
        })

        return descriptors.toArray(FoldingDescriptor.EMPTY)
    }

    /**
     * Gets the Simple Language 'value' string corresponding to the 'key'
     *
     * @param node Node corresponding to PsiLiteralExpression containing a string in the format
     * SIMPLE_PREFIX_STR + SIMPLE_SEPARATOR_STR + Key, where Key is
     * defined by the Simple language file.
     */
    override fun getPlaceholderText(node: ASTNode): String? {
        if (node.psi is PsiLiteralExpression) {
            val text = PsiLiteralUtil.getStringLiteralContent(node.psi as PsiLiteralExpression) ?: return null

            val key = text

            return key.substring("simple:".length)
            //      SimpleProperty simpleProperty = ContainerUtil.getOnlyItem(
//          SimpleUtil.findProperties(psiLiteralExpression.getProject(), key)
//      );
//      if (simpleProperty == null) {
//        return StringUtil.THREE_DOTS;
//      }
//
//      String propertyValue = simpleProperty.getValue();
//      // IMPORTANT: keys can come with no values, so a test for null is needed
//      // IMPORTANT: Convert embedded \n to backslash n, so that the string will look
//      // like it has LF embedded in it and embedded " to escaped "
//      if (propertyValue == null) {
//        return StringUtil.THREE_DOTS;
//      }
//
//      return propertyValue.replaceAll("\n", "\\n").replaceAll("\"", "\\\\\"");
        }

        return null
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }
}
