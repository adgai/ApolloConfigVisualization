package com.adgainai.apolloconfigvisualization.action


import apollo.NamespaceBO
import com.adgainai.apolloconfigvisualization.config.ApolloViewConfiguration
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
import java.util.Arrays


class RefreshApolloConfigAction : AnAction("refresh Apollo Config Visualization") {
    private var lastClickTime: Long = 0
    val cookieJar = LocalCookieJar()

    val client = OkHttpClient().newBuilder()
        .followRedirects(false) //禁制OkHttp的重定向操作，我们自己处理重定向
        .followSslRedirects(false)
        .cookieJar(cookieJar) //为OkHttp设置自动携带Cookie的功能
        .build();

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

      loginAndGetCookie(configuration.loginUrl,configuration.account,configuration.password)

        sendGetRequestWithOkHttp(configuration, project)

    }

    private fun loginAndGetCookie(loginUrl: String, username: String, password: String){
        if (StringUtils.isBlank(loginUrl) || StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return
        }
         try {

            val formBody = FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build()

            val request = Request.Builder()
                .url(loginUrl)
                .post(formBody)
                .build()

            client.newCall(request).execute()

        } catch (_: Exception) {
        }

    }

    override fun update(event: AnActionEvent) {
        // Optionally, disable the button if needed
        event.presentation.isEnabled = System.currentTimeMillis() - lastClickTime >= 30000
    }

    fun sendGetRequestWithOkHttp(config: ApolloViewConfiguration, project: Project) {

        var envToKeyToValue: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

        config.keyValues.forEach { kv ->
            val env = kv.key
            val url = kv.value
            val builder = Request.Builder()
//                .header("Cookie",cookie)
                .url(url.toString())
            val configuration = ApolloViewConfiguration.getInstance(project)

            if (StringUtils.isNotBlank(configuration.cookie)) {
                builder.header("Cookie",configuration.cookie)
            }

            val request = builder
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
