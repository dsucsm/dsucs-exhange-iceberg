package org.dsucs.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class CmdBasicOs extends Cmd {
    private CmdBasicOs() {
    }

    public static CmdBasicOs getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void execute() {
        try {
            executeBasicCommand();
        } catch (IOException | InterruptedException ex) {
            System.err.println("Error: " + ex.getMessage());
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

    private void executeBasicCommand() throws IOException, InterruptedException {
        String commandString = this.getCmdLine();
        List<String> commandArgs = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            commandArgs.addAll(List.of("cmd.exe", "/c", commandString));
        } else {
            commandArgs.addAll(List.of("/bin/sh", "-c", commandString));
        }

        ProcessBuilder builder = new ProcessBuilder(commandArgs);
        // Combine stderr into stdout so we can capture everything easily
        builder.redirectErrorStream(true);

        Process process = builder.start();

        // Read the combined stream (stdout + stderr)
        try (java.io.InputStream in = process.getInputStream()) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                System.out.write(buffer, 0, n);
            }
        }

        int exitCode = process.waitFor();
        System.out.println("\nProcess finished with exit code: " + exitCode);
    }

    private static class Holder {
        private static final CmdBasicOs INSTANCE = new CmdBasicOs();
    }
}
