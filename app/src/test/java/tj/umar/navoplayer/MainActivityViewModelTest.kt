package tj.umar.navoplayer

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.ThemeMode
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.domain.usecase.ObserveThemeModeUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository

class MainActivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSettingsRepository()

    private fun viewModel() = MainActivityViewModel(ObserveThemeModeUseCase(repository))

    @Test
    fun `starts loading before anyone collects`() {
        assertEquals(MainActivityUiState.Loading, viewModel().uiState.value)
    }

    @Test
    fun `emits stored theme mode and follows changes`() = runTest {
        repository.emit(UserSettings(themeMode = ThemeMode.Light))
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(MainActivityUiState.Ready(ThemeMode.Light), awaitItem())

            repository.setThemeMode(ThemeMode.System)
            assertEquals(MainActivityUiState.Ready(ThemeMode.System), awaitItem())
        }
    }

    @Test
    fun `read failure falls back to default theme`() = runTest {
        repository.observeError = IllegalStateException("disk")
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(MainActivityUiState.Ready(ThemeMode.Default), awaitItem())
        }
    }
}
