package com.adgainai.apolloconfigvisualization

import com.adgainai.apolloconfigvisualization.config.ApolloViewConfiguration
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.*
import org.apache.commons.collections.MapUtils
import org.apache.commons.lang3.StringUtils


class MyFoldingBuilder : FoldingBuilderEx() {

    lateinit var apolloClient: ApolloOpenApiClient
    override fun buildFoldRegions(p0: PsiElement, p1: Document, p2: Boolean): Array<FoldingDescriptor> {
        return buildFoldRegions(p0.node, p1);
    }

    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        val root = node.psi
        val project = root.project

//        val settings = MyPluginProjectSettings.getInstance(project)
//        var splitMethodSignatureList = settings.methodSignature.split(",").filter { s -> StringUtils.isNotBlank(s) }
//        if (splitMethodSignatureList.size == 0) {
        var splitMethodSignatureList = arrayListOf(
            "configUtils.getBool",
            "configUtils.getBoolean",
            "configHolder.getBool",
            "configHolder.getBoolean",
            "configUtils.getInt",
            "configUtils.getLong",
            "configUtils.getInteger",
        );
//        }


        // 使用递归访问者遍历 PSI 树查找方法调用
        root.accept(object : JavaRecursiveElementVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)

                // 检查方法调用是否符合我们的条件（即调用指定的类和方法）
                if (splitMethodSignatureList.contains(expression.methodExpression.qualifiedName)) {
                    // 创建一个 FoldingDescriptor 并添加到列表中
                    val range = expression.textRange

                    val placeholderText = "Apollo config is: " + getPlaceholderText(expression.node)
                    val element = FoldingDescriptor(
                        expression.node,
                        range,
                        null,
                        HashSet(),
                        false,
                        placeholderText,
                        true
                    )
                    descriptors.add(element)
                }

//                val qualifiedName = expression.resolveMethod()?.containingClass?.qualifiedName
//                if (){
//
//                }

            }
        })

        // Kotlin 代码的处理
//        PsiTreeUtil.collectElementsOfType(root, KtCallExpression::class.java).forEach { callExpression ->
//            if (callExpression.calleeExpression?.text == "yourMethod" /* 需要更详细的检查来确认类名 */) {
//                val range = callExpression.textRange
//                descriptors.add(FoldingDescriptor(callExpression.node, range))
//            }
//        }

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        val element = node.psi

        val project = element.project
//        val settings = MyPluginProjectSettings.getInstance(project)

        if (element is PsiMethodCallExpression) {
            val methodCallExpression = element

            val argumentList = methodCallExpression.argumentList
            val arguments = argumentList.expressions

            val staticConstantValue = getStaticConstantValue(arguments[0])
            val defaultValue = getBooleanValue(arguments[1])

//
//            val token = settings.token
//            val url = settings.url
//            val serviceName = settings.serviceName

//            val initialized = ::apolloClient.isInitialized
//            if (!initialized) {
//                apolloClient = ApolloOpenApiClient.newBuilder().withToken(token).withPortalUrl(url).build()
//            }
//
//            var envList = settings.env.split(",").filter { s -> StringUtils.isNotBlank(s) }
//            if (envList.size == 0) {
//                envList = arrayListOf(
//                    "ndev",
//                    "qa",
//                    "prod"
//                );
//            }

            var v = ""
//            envList.forEach {
//                var apolloValue =
//                    apolloClient.getItem(
//                        serviceName,
//                        it,
//                        "default",
//                        "application",
//                        staticConstantValue.toString()
//                    )?.value
//
//
//                if (apolloValue == null) {
//                    apolloValue = defaultValue.toString()
//                }
//
//                v += "$it:$apolloValue";
//                v += " "
//            }
            val configuration = ApolloViewConfiguration.getInstance(
                project
            )
            val envToKeyToValue: Map<String, Map<String, String>> = configuration.envToKeyToValue
            if (MapUtils.isEmpty(envToKeyToValue)) {
                return ""
            }
            envToKeyToValue.keys.forEach { key ->
                val stringMap = envToKeyToValue.get(key);
                v += key
                v += ':'
                v += stringMap?.getOrDefault(staticConstantValue.toString(), defaultValue = defaultValue.toString())
                    ?: ""
                v += ";"

            }
//            envToKeyToValue.entries.forEach { kv ->
//                {
//                    val env = kv.key
//                    val kvmap = kv.value
//                    v += env
//                    v += ','
//                    v += kvmap.getOrDefault(staticConstantValue.toString(), defaultValue = defaultValue.toString())
//
//                }
//
//            }

            return v
        }

        return null
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }

    fun getStaticConstantValue(ele: PsiElement): Any? {
        if (ele is PsiReferenceExpression) {
            val resolve = ele.resolve()
            if (resolve is PsiField) {
                val field = resolve;

                if (field.hasModifierProperty(PsiModifier.STATIC) && field.hasModifierProperty(PsiModifier.FINAL)) {
                    return field.computeConstantValue()
                }
            }

        }

        return null
    }

    fun getBooleanValue(ele: PsiElement): Any? {
        if (ele is PsiLiteralExpression) {
            val value = ele.value

            return value
        }

        return null
    }
}
