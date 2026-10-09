package tj.umar.navoplayer.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.datastore.settings.StoredEqualizerSettings
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection

class EqualizerSettingsMapperTest {

    @Test
    fun `levels parse and encode`() {
        assertEquals(listOf(0, 300, -150), parseBandLevels("0,300,-150"))
        assertEquals("0,300,-150", listOf(0, 300, -150).toBandLevelsStorage())
    }

    @Test
    fun `blank or broken levels parse to empty`() {
        assertEquals(emptyList<Int>(), parseBandLevels(null))
        assertEquals(emptyList<Int>(), parseBandLevels(" "))
        assertEquals(emptyList<Int>(), parseBandLevels("1,x,3"))
    }

    @Test
    fun `preset index maps to selection`() {
        assertEquals(EqualizerPresetSelection.Preset(2), StoredEqualizerSettings(presetIndex = 2).toEqualizerSettings().preset)
        assertEquals(EqualizerPresetSelection.Custom, StoredEqualizerSettings(presetIndex = -1).toEqualizerSettings().preset)
        assertEquals(EqualizerPresetSelection.Custom, StoredEqualizerSettings().toEqualizerSettings().preset)
    }
}
