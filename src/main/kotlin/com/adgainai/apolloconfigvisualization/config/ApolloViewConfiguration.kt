package com.adgainai.apolloconfigvisualization.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import org.apache.commons.lang3.StringUtils


@Service(Service.Level.PROJECT)
@State(name = "ApolloViewConfiguration", storages = [Storage("ApolloViewConfiguration.xml")])
class ApolloViewConfiguration : PersistentStateComponent<ApolloViewConfiguration> {

    var keyValues: MutableList<KeyValue> = ArrayList()

    var cookie: String = ""

    var envToKeyToValue: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    var foldingWhenEveryOpenFile: Boolean = false

    var methodSignatures: String = ""
    var account: String = ""
    var password: String = ""

    override fun getState(): ApolloViewConfiguration? {
        return this
    }

    override fun loadState(p0: ApolloViewConfiguration) {
        XmlSerializerUtil.copyBean(p0, this)

    }

    fun addKeyValue(key: String?, value: String?) {
        val keyValue = KeyValue()
        keyValue.key = key
        keyValue.value = value
        keyValues.add(keyValue)
    }


    companion object {
        fun getInstance(project: Project): ApolloViewConfiguration {
            return project.getService(ApolloViewConfiguration::class.java)
        }
    }


    class KeyValue() {
        var key: String? = null
        var value: String? = null
    }

    fun getFoldingMethodCallExpress(): List<String> {
        return getFoldingMethodSignuture().map { it -> it.split(".")[0] }.distinct().toList()
    }

    fun getFoldingMethodSignuture(): ArrayList<String> {

        var splitMethodSignatureList: ArrayList<String>

        if (StringUtils.isNotBlank(methodSignatures)) {
            splitMethodSignatureList =
                methodSignatures.split(",")
                    .filter { s -> StringUtils.isNotBlank(s) }
                    .toCollection(arrayListOf())
            return splitMethodSignatureList
        }

        splitMethodSignatureList = arrayListOf(
            "configUtils.getBool",
            "configUtils.getBoolean",
            "configUtils.getInt",
            "configUtils.getLong",
            "configUtils.getInteger",
            "configUtils.getString",

            "configHolder.getBool",
            "configHolder.getBoolean",
            "configHolder.getInt",
            "configHolder.getLong",
            "configHolder.getInteger",
            "configHolder.getString",

            "configManager.getBool",
            "configManager.getBoolean",
            "configManager.getInt",
            "configManager.getLong",
            "configManager.getInteger",
            "configManager.getString",
        );

        return splitMethodSignatureList

    }

}
