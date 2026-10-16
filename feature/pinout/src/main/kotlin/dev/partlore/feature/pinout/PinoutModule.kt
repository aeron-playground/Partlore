package dev.partlore.feature.pinout

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pinoutModule = module { viewModel { params -> PinoutViewModel(params.get(), get(), get()) } }
