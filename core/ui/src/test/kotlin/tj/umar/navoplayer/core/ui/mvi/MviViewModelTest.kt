package tj.umar.navoplayer.core.ui.mvi

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.testing.MainDispatcherRule

class MviViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private data class CounterState(val count: Int = 0)

    private sealed interface CounterIntent {
        data object Increment : CounterIntent
        data class Emit(val message: String) : CounterIntent
    }

    private data class Message(val text: String)

    private class TestViewModel : MviViewModel<CounterState, CounterIntent, Message>(CounterState()) {
        var lastSeenCount = -1

        override fun onIntent(intent: CounterIntent) {
            when (intent) {
                CounterIntent.Increment -> {
                    setState { copy(count = count + 1) }
                    lastSeenCount = currentState.count
                }
                is CounterIntent.Emit -> sendEffect(Message(intent.message))
            }
        }
    }

    @Test
    fun `exposes initial state`() {
        assertEquals(CounterState(), TestViewModel().state.value)
    }

    @Test
    fun `setState reduces state and currentState reflects it`() = runTest {
        val viewModel = TestViewModel()
        viewModel.state.test {
            assertEquals(CounterState(0), awaitItem())
            viewModel.onIntent(CounterIntent.Increment)
            assertEquals(CounterState(1), awaitItem())
        }
        assertEquals(1, viewModel.lastSeenCount)
    }

    @Test
    fun `effect sent before collection is buffered`() = runTest {
        val viewModel = TestViewModel()
        viewModel.onIntent(CounterIntent.Emit("early"))
        viewModel.effects.test {
            assertEquals(Message("early"), awaitItem())
        }
    }

    @Test
    fun `effects are delivered once and in order`() = runTest {
        val viewModel = TestViewModel()
        viewModel.effects.test {
            viewModel.onIntent(CounterIntent.Emit("a"))
            viewModel.onIntent(CounterIntent.Emit("b"))
            assertEquals(Message("a"), awaitItem())
            assertEquals(Message("b"), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `consumed effects are not replayed to a new collector`() = runTest {
        val viewModel = TestViewModel()
        viewModel.onIntent(CounterIntent.Emit("once"))
        viewModel.effects.test {
            assertEquals(Message("once"), awaitItem())
        }
        viewModel.effects.test {
            expectNoEvents()
        }
    }
}
