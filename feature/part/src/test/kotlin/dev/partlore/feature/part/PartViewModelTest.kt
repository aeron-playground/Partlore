package dev.partlore.feature.part

import dev.partlore.core.model.ContentProblem
import dev.partlore.core.model.ContentResult
import dev.partlore.core.testing.FakeContentRepository
import dev.partlore.core.testing.MainDispatcherRule
import dev.partlore.core.testing.SampleContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PartViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test
    fun thePartArrives() {
        val vm = PartViewModel(SampleContent.devBoard.id, FakeContentRepository())
        assertEquals(ContentResult.Ok(SampleContent.devBoard), vm.state.value)
    }

    @Test
    fun anUnknownPartIsNotFound() {
        assertEquals(ContentResult.NotFound, PartViewModel("nobody/nothing", FakeContentRepository()).state.value)
    }

    @Test
    fun aFailureIsPassedOn() {
        val failed = ContentResult.Failed(ContentProblem.TooNew, "format 2")
        val repo = FakeContentRepository().apply { parts[SampleContent.devBoard.id] = failed }
        assertEquals(failed, PartViewModel(SampleContent.devBoard.id, repo).state.value)
    }
}
