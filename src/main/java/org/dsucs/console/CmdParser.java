package org.dsucs.console;

import org.dsucs.Util;

class CmdParser {
    public void prompt() {
        System.out.print("\nexchange$ ");
    }

    public Cmd parse(String cmdLine) {
        Cmd cmd;
        String command = cmdLine.toLowerCase().trim();
        if (command.matches(Util.cmdExit)) {
            return CmdExit.getInstance();
        } else if (command.matches(Util.cmdHelp)) {
            return CmdHelp.getInstance();
        } else if (command.matches(Util.cmdEnginePrefix) ||
                command.matches(Util.cmdEngineSuffix) ||
                (command.matches(Util.nonWhiteSpace) &&
                        (command.matches(Util.orderLineFormat) ||
                                Util.isFilePathValid(command))
                )) {
            cmd = CmdEngine.getInstance();
            cmd.setCmdLine(cmdLine);
            return cmd;
        } else {
            // Reusing Singleton and updating its state
            cmd = CmdBasicOs.getInstance();
            cmd.setCmdLine(cmdLine);
            return cmd;
        }
    }
}
