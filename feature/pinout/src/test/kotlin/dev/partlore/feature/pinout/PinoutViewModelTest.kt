package dev.partlore.feature.pinout

import dev.partlore.core.model.ContentResult
import dev.partlore.core.model.PinLabelMode
import dev.partlore.core.model.Rotation
import dev.partlore.core.model.ThemeSetting
import dev.partlore.core.model.UserSettings
import dev.partlore.core.testing.FakeContentRepository
import dev.partlore.core.testing.FakeUserSettingsRepository
import dev.partlore.core.testing.MainDispatcherRule
import dev.partlore.core.testing.SampleContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PinoutViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val id = SampleContent.devBoardPinout.partId

    private fun vm(settings: FakeUserSettingsRepository = FakeUserSettingsRepository()) =
        PinoutViewModel(id, FakeContentRepository(), settings)

    private fun PinoutViewModel.ready(): PinoutState = (state.value as ContentResult.Ok).value

    @Test
    fun theViewerOpensWithItsDefaults() {
        val state = vm().ready()
        assertEquals(PinLabelMode.Board, state.labelMode)
        assertEquals(Rotation.R0, state.board.rotation)
        assertEquals(false, state.keepScreenOn)
        assertEquals(false, state.pinsChecked)
        assertEquals(null, state.filter)
    }

    @Test
    fun theBenchThemeKeepsTheScreenOnUnlessTurnedOff() {
        val settings = FakeUserSettingsRepository(UserSettings(theme = ThemeSetting.Bench))
        val vm = vm(settings)
        assertEquals(true, vm.ready().keepScreenOn)
        assertEquals(true, vm.ready().bench)
        vm.onAction(PinoutAction.ToggleKeepScreenOn)
        assertEquals(false, settings.settings.value.keepScreenOn)
        assertEquals(false, vm.ready().keepScreenOn)
    }

    @Test
    fun aFilterDimsEveryOtherPin() {
        val vm = vm()
        vm.onAction(PinoutAction.Filter(PinFilter.I2c))
        assertEquals(
            SampleContent.devBoardPinout.pins.map { it.id }.toSet() - setOf("io21", "io22"),
            vm.ready().dimmed,
        )
        vm.onAction(PinoutAction.Filter(null))
        assertEquals(emptySet<String>(), vm.ready().dimmed)
    }

    @Test
    fun theLabelChoiceIsRemembered() {
        val settings = FakeUserSettingsRepository()
        val vm = vm(settings)
        vm.onAction(PinoutAction.Labels(PinLabelMode.Chip))
        assertEquals(PinLabelMode.Chip, settings.settings.value.pinLabel)
        assertEquals(PinLabelMode.Chip, vm.ready().labelMode)
    }

    @Test
    fun rotateTurnsAQuarterAndSelectOpensAPin() {
        val vm = vm()
        vm.onAction(PinoutAction.Rotate)
        assertEquals(Rotation.R90, vm.ready().board.rotation)
        vm.onAction(PinoutAction.Select("io2"))
        assertEquals("io2", vm.ready().selected?.id)
        vm.onAction(PinoutAction.Select(null))
        assertEquals(null, vm.ready().selected)
    }

    @Test
    fun redLightAndTableToggle() {
        val settings = FakeUserSettingsRepository()
        val vm = vm(settings)
        vm.onAction(PinoutAction.ToggleRedLight)
        vm.onAction(PinoutAction.ToggleTable)
        assertEquals(true, settings.settings.value.redLight)
        assertEquals(true, vm.ready().redLight)
        assertEquals(true, vm.ready().table)
    }

    @Test
    fun anUnknownPartIsNotFound() {
        val vm = PinoutViewModel("nobody/nothing", FakeContentRepository(), FakeUserSettingsRepository())
        assertEquals(ContentResult.NotFound, vm.state.value)
    }
}
