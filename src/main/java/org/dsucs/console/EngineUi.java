package org.dsucs.console;

import java.util.Scanner;

public class EngineUi {
    public static void main(String[] args) {
        // Use try-with-resources for Scanner
        try (Scanner lineScanner = new Scanner(System.in)) {
            CmdParser cmdParser = new CmdParser();
            boolean running = true;

            while (running) {
                cmdParser.prompt();
                if (!lineScanner.hasNextLine()) break;

                String inputLine = lineScanner.nextLine().trim();
                if (inputLine.isEmpty()) continue;

                Cmd cmd = cmdParser.parse(inputLine);
                cmd.execute();

                if (cmd.isExit()) running = false;
            }
        }
        System.out.println("EngineUi terminated.");
    }
}
