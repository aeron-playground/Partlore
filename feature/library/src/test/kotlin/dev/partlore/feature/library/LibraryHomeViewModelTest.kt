package dev.partlore.feature.library

import dev.partlore.core.model.ContentProblem
import dev.partlore.core.model.ContentResult
import dev.partlore.core.testing.FakeContentRepository
import dev.partlore.core.testing.MainDispatcherRule
import dev.partlore.core.testing.SampleContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LibraryHomeViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test
    fun theLibraryArrivesFromTheRepository() {
        val vm = LibraryHomeViewModel(FakeContentRepository())
        assertEquals(ContentResult.Ok(SampleContent.library), vm.state.value)
    }

    @Test
    fun aFailureIsPassedOn() {
        val failed = ContentResult.Failed(ContentProblem.Missing, "no pack")
        val vm = LibraryHomeViewModel(FakeContentRepository(library = failed))
        assertEquals(failed, vm.state.value)
    }
}
