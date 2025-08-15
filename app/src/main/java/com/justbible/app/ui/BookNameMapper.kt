package com.justbible.app.ui

import android.content.Context
import org.json.JSONObject

object BookNameMapper {
    private var osisToKo: Map<String, String>? = null
    private var englishToKo: Map<String, String>? = null

    private fun ensureLoaded(context: Context) {
        if (osisToKo != null && englishToKo != null) return
        val am = context.assets
        am.open("book_names_ko.json").use { input ->
            val text = input.reader(Charsets.UTF_8).readText()
            val root = JSONObject(text)
            val byOsis = root.getJSONObject("byOsis")
            val byEnglish = root.getJSONObject("byEnglish")
            val o = mutableMapOf<String, String>()
            val e = mutableMapOf<String, String>()
            byOsis.keys().forEach { k -> o[k] = byOsis.getString(k) }
            byEnglish.keys().forEach { k -> e[k] = byEnglish.getString(k) }
            osisToKo = o
            englishToKo = e
        }
    }

    fun displayName(context: Context, osis: String?, englishFull: String?): String? {
        ensureLoaded(context)
        val o = osis?.let { osisToKo?.get(it) }
        if (o != null) return o
        val e = englishFull?.let { englishToKo?.get(it) }
        return e
    }
}


