package dev.partlore.core.designsystem.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ElevationTest {
    private val levels = PartloreElevation.all

    @Test
    fun levelsAreZeroToFour() = assertEquals(listOf(0, 1, 2, 3, 4), levels.map { it.level })

    @Test
    fun atMostThreeLayers() = levels.forEach { assertTrue("e${it.level}", it.layers.size <= 3) }

    @Test
    fun everyLayerFallsDownRightAtOneToTwo() = levels.flatMap {
        it.layers
    }.forEach { assertEquals("x:y must be 1:2 for $it", it.x.value * 2, it.y.value, 0.001f) }

    @Test
    fun higherLevelsHaveBiggerOffsetsAndBlurAndNoHigherAlpha() {
        levels.drop(1).zipWithNext().forEach { (low, high) ->
            low.layers.zip(high.layers).forEachIndexed { i, (a, b) ->
                val where = "layer $i, e${low.level} → e${high.level}"
                assertTrue("offset must grow at $where", b.y > a.y)
                assertTrue("blur must grow at $where", b.blur > a.blur)
                assertTrue("alpha must not grow at $where", b.alpha <= a.alpha)
            }
        }
    }
}
