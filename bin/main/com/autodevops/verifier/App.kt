package com.autodevops.verifier

import com.autodevops.verifier.commands.DoctorCommand
import com.autodevops.verifier.commands.InitCommand
import com.autodevops.verifier.commands.RunCommand
import com.autodevops.verifier.commands.TokenUsageCommand
import picocli.CommandLine
import picocli.CommandLine.Command
import kotlin.system.exitProcess

@Command(
    name = "verifier",
    mixinStandardHelpOptions = true,
    version = ["0.1.0"],
    description = ["Verifier CLI tool"],
    subcommands = [
        InitCommand::class,
        RunCommand::class,
        TokenUsageCommand::class,
        DoctorCommand::class
    ]
)
class App : Runnable {
    override fun run() {
        CommandLine.usage(this, System.out)
    }
}

fun main(args: Array<String>) {
    exitProcess(CommandLine(App()).execute(*args))
}
