package org.example;

import org.example.generator.DataGenerator;
import org.example.solver.EquationSolver;
import picocli.CommandLine;

@CommandLine.Command(name = "Mirage",
        version = {"${COMMAND-NAME} 0.1.0",
                "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                "OS: ${os.name} ${os.version} ${os.arch}"},
        description = "tool for generating test database", sortOptions = false,
        subcommands = {EquationSolver.class, DataGenerator.class},
        mixinStandardHelpOptions = true, usageHelpAutoWidth = true,
        header = {
                "@|green  _  _ _ ____ ____ ____ ____ |@",
                "@|green  |\\/| | |__/ |__| | __ |___  |@",
                "@|green  |  | | |  \\ |  | |__] |___  |@"}
)
public class MainAPP {
    public static void main(String... args) {
        int exitCode = new CommandLine(new MainAPP()).execute(args);
        System.exit(exitCode);
    }
}
