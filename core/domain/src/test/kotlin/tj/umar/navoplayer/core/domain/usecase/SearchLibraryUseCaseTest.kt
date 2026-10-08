package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.SearchResults
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository
import tj.umar.navoplayer.core.testing.data.TestSearchTracks
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class SearchLibraryUseCaseTest {

    private val repository = FakeTrackRepository()

    private fun TestScope.useCase(): SearchLibraryUseCase {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return SearchLibraryUseCase(ObserveLibraryUseCase(ObserveTracksUseCase(repository, FakeSettingsRepository()), dispatcher), dispatcher)
    }

    @Test
    fun `emits results for each query with one library subscription`() = runTest {
        val queries = MutableStateFlow("black")

        useCase()(queries).test {
            repository.emit(TestSearchTracks.library)
            assertEquals(listOf("Blackbird", "Back in Black"), awaitItem().tracks.map { it.title })

            queries.value = "ёлка"
            assertEquals(listOf("Ёлка"), awaitItem().tracks.map { it.title })

            assertEquals(1, repository.observeCalls)
        }
    }

    @Test
    fun `library change refreshes results`() = runTest {
        val queries = MutableStateFlow("black")

        useCase()(queries).test {
            repository.emit(TestSearchTracks.library)
            awaitItem()
            repository.emit(listOf(TestSearchTracks.blackbird))
            assertEquals(listOf("Blackbird"), awaitItem().tracks.map { it.title })
        }
    }

    @Test
    fun `equal trimmed queries are deduplicated`() = runTest {
        val queries = MutableStateFlow("black")

        useCase()(queries).test {
            repository.emit(TestSearchTracks.library)
            awaitItem()
            queries.value = " black "
            expectNoEvents()
        }
    }

    @Test
    fun `blank query emits empty results`() = runTest {
        useCase()(MutableStateFlow("   ")).test {
            repository.emit(TestSearchTracks.library)
            assertEquals(SearchResults.empty(""), awaitItem())
        }
    }

    @Test
    fun `repository error propagates`() = runTest {
        repository.error = IllegalStateException("scan failed")

        useCase()(MutableStateFlow("x")).test {
            assertTrue(awaitError() is IllegalStateException)
        }
    }
}
