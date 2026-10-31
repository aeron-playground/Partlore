package dev.partlore.tools.importer

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(ImporterCli(System.out, System.err).run(args))
}
