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

    var envToKeyToValue: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    var projectName: String = ""

    override fun getState(): ApolloViewConfiguration? {
        return this
    }

    override fun loadState(p0: ApolloViewConfiguration) {
        XmlSerializerUtil.copyBean(p0, this)
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
