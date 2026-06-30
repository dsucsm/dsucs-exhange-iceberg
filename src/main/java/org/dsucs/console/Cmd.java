package org.dsucs.console;

abstract class Cmd {
    private String cmdLine;

    public abstract void execute();

    public boolean isExit() {
        return false;
    } // Helper to avoid 'instanceof'

    public String getCmdLine() {
        return cmdLine;
    }

    public void setCmdLine(String cmdLine) {
        this.cmdLine = cmdLine;
    }
}