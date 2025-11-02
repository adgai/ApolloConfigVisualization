package com.adgainai.apolloconfigvisualization.config

import com.intellij.openapi.components.*
import org.apache.commons.lang3.StringUtils

@State(
    name = "CodemanGlobalSettings",
    storages = [Storage("CodemanGlobalSettings.xml")]
)
@Service(Service.Level.APP)
class CodemanGlobalSettings : PersistentStateComponent<CodemanGlobalSettings.State> {

    companion object {
        val instance: CodemanGlobalSettings
            get() = service()
    }

    data class State(
        var keyValues: MutableList<KeyValue> = ArrayList(),

        var cookie: String = "",

        var foldingWhenEveryOpenFile: Boolean = false,

        var methodSignatures: String = "",
        var account: String = "",
        var password: String = ""
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    class KeyValue() {
        var key: String? = null
        var value: String? = null
    }


    fun addKeyValue(key: String?, value: String?) {
        val keyValue = KeyValue()
        keyValue.key = key
        keyValue.value = value
        state.keyValues.add(keyValue)
    }

    fun getFoldingMethodCallExpress(): List<String> {
        return getFoldingMethodSignuture().map { it -> it.split(".")[0] }.distinct().toList()
    }

    fun getFoldingMethodSignuture(): ArrayList<String> {

        var splitMethodSignatureList: ArrayList<String>

        if (StringUtils.isNotBlank(state.methodSignatures)) {
            splitMethodSignatureList =
                state.methodSignatures.split(",")
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
