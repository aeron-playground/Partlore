package dev.partlore.app

import android.content.res.AssetManager
import dev.partlore.core.content.BundledPack

private const val PACK_ASSETS = "content"

/** The content pack the build put into the APK's assets (see CopyPackTask). */
internal fun AssetManager.bundledPack(): BundledPack = BundledPack(
    readManifest = { open("$PACK_ASSETS/manifest.json").bufferedReader().use { it.readText() } },
    open = { name -> open("$PACK_ASSETS/$name") },
)
