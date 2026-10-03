package dev.partlore.feature.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.partlore.core.designsystem.components.PlButton
import dev.partlore.core.designsystem.components.PlChip
import dev.partlore.core.designsystem.components.PlTextButton
import dev.partlore.core.designsystem.theme.PartloreSprings
import dev.partlore.core.designsystem.theme.PartloreTheme
import dev.partlore.core.model.BuildInterest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private const val PAGE_COUNT = 4
private const val INTERESTS_PAGE = 3

@Composable
fun OnboardingRoute(modifier: Modifier = Modifier, viewModel: OnboardingViewModel = koinViewModel()) {
    OnboardingScreen(onFinish = viewModel::finish, modifier = modifier)
}

@Composable
fun OnboardingScreen(onFinish: (Set<BuildInterest>) -> Unit, modifier: Modifier = Modifier, initialPage: Int = 0) {
    val pager = rememberPagerState(initialPage = initialPage, pageCount = { PAGE_COUNT })
    var interests by rememberSaveable { mutableStateOf(setOf<BuildInterest>()) }
    val scope = rememberCoroutineScope()
    val motion = PartloreTheme.motion
    val pageAlpha = remember { Animatable(1f) }
    val spacing = PartloreTheme.spacing
    Column(modifier.fillMaxSize().background(PartloreTheme.colors.bg).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = spacing.space8), horizontalArrangement = Arrangement.End) {
            PlTextButton(stringResource(R.string.onboarding_skip), onClick = { onFinish(emptySet()) })
        }
        HorizontalPager(
            state = pager,
            modifier = Modifier.weight(1f).graphicsLayer {
                alpha = pageAlpha.value
            },
        ) { page ->
            // Large text on a small phone can make a page taller than the screen.
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (page == INTERESTS_PAGE) {
                    InterestsPage(selected = interests, onToggle = {
                        interests =
                            if (it in interests) interests - it else interests + it
                    })
                } else {
                    InfoPage(page)
                }
            }
        }
        PageDots(current = pager.currentPage, modifier = Modifier.align(Alignment.CenterHorizontally))
        val last = pager.currentPage == PAGE_COUNT - 1
        PlButton(
            text = stringResource(if (last) R.string.onboarding_start else R.string.onboarding_continue),
            onClick = {
                if (last) {
                    onFinish(interests)
                } else {
                    scope.launch {
                        val next = pager.currentPage + 1
                        if (motion.reduced) {
                            // Reduced motion: cross-fade to the next page instead of sliding.
                            pageAlpha.animateTo(0f, motion.fade())
                            pager.scrollToPage(next)
                            pageAlpha.animateTo(1f, motion.fade())
                        } else {
                            pager.animateScrollToPage(next, animationSpec = motion.spring(PartloreSprings.drift))
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(spacing.space16),
        )
    }
}

@Composable
private fun InfoPage(page: Int, modifier: Modifier = Modifier) {
    val (title, body) =
        when (page) {
            0 -> R.string.onboarding_1_title to R.string.onboarding_1_body
            1 -> R.string.onboarding_2_title to R.string.onboarding_2_body
            else -> R.string.onboarding_3_title to R.string.onboarding_3_body
        }
    PageText(stringResource(title), stringResource(body), modifier)
}

@Composable
private fun InterestsPage(
    selected: Set<BuildInterest>,
    onToggle: (BuildInterest) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        PageText(stringResource(R.string.onboarding_4_title), stringResource(R.string.onboarding_4_body))
        FlowRow(
            modifier = Modifier.padding(horizontal = PartloreTheme.spacing.space24),
            horizontalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space8),
        ) {
            BuildInterest.entries.forEach { interest ->
                PlChip(stringResource(interest.labelRes()), selected = interest in selected, onClick = {
                    onToggle(interest)
                })
            }
        }
    }
}

@Composable
private fun PageText(title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(PartloreTheme.spacing.space24)) {
        Text(title, style = PartloreTheme.typography.displayM, color = PartloreTheme.colors.textPrimary)
        Text(
            body,
            style = PartloreTheme.typography.bodyL,
            color = PartloreTheme.colors.textSecondary,
            modifier = Modifier.padding(top = PartloreTheme.spacing.space12),
        )
    }
}

@Composable
private fun PageDots(current: Int, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.onboarding_page, current + 1, PAGE_COUNT)
    Row(
        modifier = modifier.padding(PartloreTheme.spacing.space8).semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(PartloreTheme.spacing.space8),
    ) {
        repeat(PAGE_COUNT) { index ->
            Box(
                Modifier
                    .size(PartloreTheme.spacing.space8)
                    .background(
                        if (index == current) PartloreTheme.colors.primary else PartloreTheme.colors.outline,
                        CircleShape,
                    ),
            )
        }
    }
}

private fun BuildInterest.labelRes(): Int = when (this) {
    BuildInterest.Esp32 -> R.string.interest_esp32
    BuildInterest.Arduino -> R.string.interest_arduino
    BuildInterest.Pico -> R.string.interest_pico
    BuildInterest.Stm32 -> R.string.interest_stm32
    BuildInterest.Sensors -> R.string.interest_sensors
}
