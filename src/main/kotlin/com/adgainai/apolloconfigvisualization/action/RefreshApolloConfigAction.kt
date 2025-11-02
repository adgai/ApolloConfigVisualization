package com.adgainai.apolloconfigvisualization.action


import apollo.NamespaceBO
import com.adgainai.apolloconfigvisualization.config.ApolloViewConfiguration
import com.adgainai.apolloconfigvisualization.config.CodemanGlobalSettings
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.TypeReference
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import okhttp3.*
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils


class RefreshApolloConfigAction : AnAction("refresh Apollo Config Visualization") {
    private var lastClickTime: Long = 0

    var apolloHostToClient: MutableMap<String, OkHttpClient> = mutableMapOf()

    //CookieJar是用于保存Cookie的
    class LocalCookieJar : CookieJar {
        var cookies: List<Cookie>? = null

        override fun loadForRequest(arg0: HttpUrl): List<Cookie> {
            if (cookies != null) return cookies as List<Cookie>
            return ArrayList()
        }

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            this.cookies = cookies;
        }


    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(event: AnActionEvent) {
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - lastClickTime < 5000) {
//            event.project?.let { showNotification(it, "5s可刷新一次") }
//            return
//        }
//
//        lastClickTime = currentTime

        val project = event.project ?: return
        val projectConfig = ApolloViewConfiguration.getInstance(project)
        val globalConfig = CodemanGlobalSettings.instance.state

        val keyValues = globalConfig.keyValues

        if (CollectionUtils.isEmpty(keyValues)) {
            Messages.showMessageDialog(
                "没有Apollo环境以及相应的获取方式，Settings-> Tools -> ApolloConfig setting ,填入环境名，以及相应环境获取配置的链接",
                "Configuration",
                Messages.getInformationIcon()
            )
            return
        }

        // login to get cookie
        preLoginToGetCookie(globalConfig)

        // get every env config
        sendGetRequestWithOkHttp(projectConfig, globalConfig, project)

    }

    override fun update(event: AnActionEvent) {
        // Optionally, disable the button if needed
        event.presentation.isEnabled = System.currentTimeMillis() - lastClickTime >= 30000
    }

    fun preLoginToGetCookie(config: CodemanGlobalSettings.State) {
        config.keyValues.forEach { kv ->

            val url = kv.value
            if (StringUtils.isBlank(url)) {
                return@forEach
            }

            val host = url?.let { it.split("apps")[0] } ?: return@forEach

            val loginUrl = host + "signin"

            val thisHostClient = apolloHostToClient.getOrPut(host) {
                OkHttpClient().newBuilder()
                    .followRedirects(false) //禁制OkHttp的重定向操作，我们自己处理重定向
                    .followSslRedirects(false)
                    .cookieJar(LocalCookieJar()) //为OkHttp设置自动携带Cookie的功能
                    .build();
            }

            try {

                val formBody = FormBody.Builder()
                    .add("username", config.account)
                    .add("password", config.password)
                    .build()

                val request = Request.Builder()
                    .url(loginUrl)
                    .post(formBody)
                    .build()

                thisHostClient.newCall(request).execute()

            } catch (_: Exception) {
            }


        }
    }

    fun sendGetRequestWithOkHttp(
        projectConfig: ApolloViewConfiguration,
        config: CodemanGlobalSettings.State,
        project: Project
    ) {

        var envToKeyToValue: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

        config.keyValues.forEach { kv ->
            val env = kv.key
            val url = kv.value

            val pn = projectConfig.projectName
                ?.takeIf { it.isNotBlank() }
                ?: project.name

            val builder = Request.Builder()
                .url(String.format(url.toString(), pn))

            val host = url?.let { it.split("apps")[0] } ?: return@forEach

            val hostClient = apolloHostToClient.get(host)

            if (hostClient == null) {
                return@forEach
            }

            if (StringUtils.isNotBlank(config.cookie)) {
                builder.header("Cookie", config.cookie)
            }

            val request = builder
                .build()

            hostClient.newCall(request).execute().use { response ->

                if (!response.isSuccessful) {

                    val content = "加载环境 $env Apollo失败"
                    showNotification(project, content)

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
                        showNotification(project, content)
                    }
                }
            }

            projectConfig.envToKeyToValue = envToKeyToValue

        };

    }


}
