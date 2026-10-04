package dev.partlore.tools.content.diag

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DiagnosticsTest {
    // Parts are loaded in parallel, so problems arrive from several threads at once.
    @Test
    fun problemsReportedFromManyThreadsAreAllKept() {
        val diagnostics = Diagnostics()
        val pool = Executors.newFixedThreadPool(THREADS)
        repeat(THREADS) { thread ->
            pool.execute {
                repeat(PER_THREAD) { diagnostics.error(Pos("t$thread.yaml", it + 1, 1), "problem") }
            }
        }
        pool.shutdown()
        check(pool.awaitTermination(1, TimeUnit.MINUTES))
        assertEquals(THREADS * PER_THREAD, diagnostics.all.size)
    }

    private companion object {
        const val THREADS = 8
        const val PER_THREAD = 5_000
    }
}
