package com.adgainai.apolloconfigvisualization

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.adgainai.apolloconfigvisualization.MyPluginProjectSettings",
    storages = [Storage("apolloconfigvisualization_MyPluginSettings.xml")]
)
class MyPluginProjectSettings : PersistentStateComponent<MyPluginProjectSettings> {
    var token: String = ""
    var url: String = ""
    var serviceName: String = ""
    var methodSignature: String = ""
    var env: String = ""


    companion object {
        fun getInstance(project: Project): MyPluginProjectSettings {
            return project.getService(MyPluginProjectSettings::class.java)
        }
    }

    override fun getState(): MyPluginProjectSettings? {
        return this
    }

    override fun loadState(state: MyPluginProjectSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
