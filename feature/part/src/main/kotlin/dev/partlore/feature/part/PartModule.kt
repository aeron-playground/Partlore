package dev.partlore.feature.part

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val partModule = module { viewModel { params -> PartViewModel(params.get(), get()) } }
