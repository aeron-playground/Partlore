package dev.partlore.app

import dev.partlore.core.content.ContentRepository
import dev.partlore.core.content.PackContentRepository
import dev.partlore.core.content.PackInstaller
import dev.partlore.core.userdata.userDataModule
import dev.partlore.feature.library.libraryModule
import dev.partlore.feature.onboarding.onboardingModule
import dev.partlore.feature.part.partModule
import dev.partlore.feature.settings.settingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.io.File

private const val PACK_DIR = "content"

private val appModule = module { viewModelOf(::MainViewModel) }

// The bundled pack is copied into app storage on first use; the repository waits for the copy.
private val contentModule =
    module {
        single<ContentRepository> {
            val context = androidContext()
            val installer = PackInstaller(File(context.filesDir, PACK_DIR))
            val bundled = context.assets.bundledPack()
            PackContentRepository(install = { force -> installer.install(bundled, force) })
        }
    }

val appModules =
    listOf(userDataModule, contentModule, settingsModule, onboardingModule, libraryModule, partModule, appModule)
