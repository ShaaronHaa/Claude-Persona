package com.claude.persona.model

import com.google.gson.annotations.SerializedName

data class Persona(
    val id: String,
    val category: String,
    val work: String,
    val cp: String,
    @SerializedName("personaName")
    val personaName: String,
    @SerializedName("userName")
    val userName: String,
    val style: String,
    @SerializedName("callName")
    val callName: String
) {
    fun getDisplayName(): String = "$cp ($work)"

    fun getPromptSection(): String = """
## 🎭 Claude人设（由Claude Persona插件生成）

$style

*当前人设：$cp | 来源：$work | 切换时间：${java.time.LocalDate.now()}*
""".trimIndent()
}