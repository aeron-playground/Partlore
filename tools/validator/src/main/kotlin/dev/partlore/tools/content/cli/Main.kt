package dev.partlore.tools.content.cli

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(ValidatorCli(System.out, System.getenv("GITHUB_ACTIONS") == "true").run(args))
}
