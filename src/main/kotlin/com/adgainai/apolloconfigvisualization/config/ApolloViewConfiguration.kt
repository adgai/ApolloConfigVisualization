package com.adgainai.apolloconfigvisualization.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil


@Service(Service.Level.PROJECT)
@State(name = "ApolloViewConfiguration", storages = [Storage("ApolloViewConfiguration.xml")])
class ApolloViewConfiguration : PersistentStateComponent<ApolloViewConfiguration> {

    var keyValues: MutableList<KeyValue> = ArrayList()

    var cookie: String = ""

    var envUrl: MutableMap<String, String> = mutableMapOf()

    var envToKeyToValue: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    var foldingWhenEveryOpenFile: Boolean = false


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
}
