package dev.partlore.tools.packer

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(PackerCli(System.out, System.getenv("GITHUB_ACTIONS") == "true").run(args))
}
