package br.com.remediohoracerta.core.i18n

data class SupportedLanguage(
    val languageTag: String,
    val autonym: String,
)

object SupportedLanguages {
    val all = listOf(
        SupportedLanguage("pt-BR", "Português (Brasil)"),
        SupportedLanguage("en", "English"),
        SupportedLanguage("es", "Español"),
        SupportedLanguage("fr", "Français"),
        SupportedLanguage("de", "Deutsch"),
        SupportedLanguage("it", "Italiano"),
        SupportedLanguage("nl", "Nederlands"),
        SupportedLanguage("pl", "Polski"),
        SupportedLanguage("ru", "Русский"),
        SupportedLanguage("uk", "Українська"),
        SupportedLanguage("tr", "Türkçe"),
        SupportedLanguage("ar", "العربية"),
        SupportedLanguage("hi", "हिन्दी"),
        SupportedLanguage("bn", "বাংলা"),
        SupportedLanguage("zh-CN", "简体中文"),
        SupportedLanguage("ja", "日本語"),
        SupportedLanguage("ko", "한국어"),
        SupportedLanguage("id", "Bahasa Indonesia"),
        SupportedLanguage("vi", "Tiếng Việt"),
        SupportedLanguage("th", "ไทย"),
    )

    fun findByTag(tag: String): SupportedLanguage? {
        val normalized = tag.replace('_', '-').lowercase().let {
            if (it == "in" || it.startsWith("in-")) it.replaceFirst("in", "id") else it
        }
        return all.firstOrNull { language ->
            val candidate = language.languageTag.lowercase()
            candidate == normalized ||
                (normalized.substringBefore('-') == candidate.substringBefore('-') &&
                    candidate != "pt-br" && candidate != "zh-cn")
        }
    }
}

