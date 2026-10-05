package dev.partlore.feature.library

import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val libraryModule =
    module {
        viewModelOf(::LibraryHomeViewModel)
        viewModel { params -> CategoryViewModel(params.get(), get()) }
    }
