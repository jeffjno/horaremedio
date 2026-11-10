package br.com.remediohoracerta.core.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportedLanguagesTest {
    @Test
    fun `exposes twenty unique languages`() {
        val languages = SupportedLanguages.all

        assertEquals(20, languages.size)
        assertEquals(20, languages.map { it.languageTag }.toSet().size)
        assertTrue(languages.all { it.languageTag.isNotBlank() && it.autonym.isNotBlank() })
    }

    @Test
    fun `matches regional system tags when language fallback is safe`() {
        assertEquals("en", SupportedLanguages.findByTag("en-US")?.languageTag)
        assertEquals("es", SupportedLanguages.findByTag("es-MX")?.languageTag)
        assertEquals("id", SupportedLanguages.findByTag("in")?.languageTag)
        assertNotNull(SupportedLanguages.findByTag("pt-BR"))
        assertNotNull(SupportedLanguages.findByTag("zh-CN"))
    }
}

