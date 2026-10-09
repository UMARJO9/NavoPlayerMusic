package tj.umar.navoplayer.feature.settings.equalizer

import org.junit.Assert.assertEquals
import org.junit.Test

class EqualizerFormattingTest {

    @Test
    fun `frequencies switch to kilohertz at one thousand`() {
        assertEquals(FrequencyLabel.Hertz(999), frequencyLabel(999))
        assertEquals(FrequencyLabel.Kilohertz("1"), frequencyLabel(1_000))
        assertEquals(FrequencyLabel.Kilohertz("3.6"), frequencyLabel(3_600))
        assertEquals(FrequencyLabel.Kilohertz("14"), frequencyLabel(14_000))
    }

    @Test
    fun `decibels carry sign and optional decimal`() {
        assertEquals("+3", formatDecibels(300))
        assertEquals("0", formatDecibels(0))
        assertEquals("−4.5", formatDecibels(-450))
    }
}
