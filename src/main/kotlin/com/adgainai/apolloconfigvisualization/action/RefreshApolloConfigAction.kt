package com.adgainai.apolloconfigvisualization.action


import apollo.NamespaceBO
import com.adgainai.apolloconfigvisualization.config.ApolloViewConfiguration
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.TypeReference
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.collections.CollectionUtils

class RefreshApolloConfigAction : AnAction("refresh Apollo Config Visualization") {
    private var lastClickTime: Long = 0

    override fun actionPerformed(event: AnActionEvent) {
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - lastClickTime < 5000) {
//            event.project?.let { showNotification(it, "5s可刷新一次") }
//            return
//        }
//
//        lastClickTime = currentTime

        val project = event.project ?: return
        val configuration = ApolloViewConfiguration.getInstance(project)
        val keyValues = configuration.keyValues

        if (CollectionUtils.isEmpty(keyValues)) {
            Messages.showMessageDialog(
                "没有Apollo环境以及相应的获取方式，Settings-> Tools -> ApolloConfig setting ,填入环境名，以及相应环境获取配置的链接",
                "Configuration",
                Messages.getInformationIcon()
            )
            return
        }

//        val message = keyValues.joinToString("\n") { "Key: ${it.key}, Value: ${it.value}" }
//        Messages.showMessageDialog(
//            message,
//            "Configuration",
//            Messages.getInformationIcon()
//        )


        sendGetRequestWithOkHttp(configuration, project)

    }

    override fun update(event: AnActionEvent) {
        // Optionally, disable the button if needed
        event.presentation.isEnabled = System.currentTimeMillis() - lastClickTime >= 30000
    }

    fun sendGetRequestWithOkHttp(config: ApolloViewConfiguration, project: Project) {
        val client = OkHttpClient()
        var envToKeyToValue: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

        config.keyValues.forEach { kv ->
            val env = kv.key
            val url = kv.value
            val request = Request.Builder()
                .header("Cookie", config.cookie)
                .url(url.toString())
                .build()

            client.newCall(request).execute().use { response ->
//                showNotification(project, "加载Apollo配置")
                if (!response.isSuccessful) {

//                    Messages.showMessageDialog(
//                        String.format("环境：%s，加载配置失败", env),
//                        "ApolloView",
//                        Messages.getInformationIcon()
//                    )
                    val content = "加载环境 $env Apollo失败"
                    showNotification(project,content)

                } else {
                    val body = response.body
                    if (body != null) {
                        val content = "加载环境 $env Apollo成功"
                        showNotification(project, content)
                        val string = body.string()
                        val typeReference = object : TypeReference<List<NamespaceBO>>() {}

                        val parseObject: List<NamespaceBO> = JSON.parseObject(string, typeReference)

                        val orDefault: MutableMap<String, String> =
                            envToKeyToValue.computeIfAbsent(env.toString(), { mutableMapOf() })
                        for (openNamespaceDTO in parseObject) {
                            val associate = openNamespaceDTO.items.associate { it.item.key to it.item.value }
                            orDefault.putAll(associate)
                        }
                    } else {
                        val content = "加载环境 $env Apollo失败"
                        showNotification(project,content)
                    }
                }
            }

            config.envToKeyToValue = envToKeyToValue

        };

    }


}
