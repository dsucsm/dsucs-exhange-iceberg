package org.dsucs.console;

class CmdExit extends Cmd {
    private CmdExit() {
    }

    public static CmdExit getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean isExit() {
        return true;
    }

    @Override
    public void execute() {
        System.out.println("Shutting down ORDER MATCHING ENGINE...");
    }

    private static class Holder {
        private static final CmdExit INSTANCE = new CmdExit();
    }
}
