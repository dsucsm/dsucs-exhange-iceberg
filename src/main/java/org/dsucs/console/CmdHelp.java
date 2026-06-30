package org.dsucs.console;

public class CmdHelp extends Cmd {
    private CmdHelp() {
    }

    public static CmdHelp getInstance() {
        return CmdHelp.Holder.INSTANCE;
    }

    @Override
    public void execute() {
        System.out.println("      #######################################################################################");
        System.out.println("      #                                                                                     #");
        System.out.println("      #                        SIMULATED ORDER MATCHING ENGINE                              #");
        System.out.println("      #                      *** Supported engine commands  ***                             #");
        System.out.println("      #  [help|h|?]<enter>       : Show this help message                                   #");
        System.out.println("      #  [cat|type|ls|dir]<enter>: Basic OS commands                                        #");
        System.out.println("      #  [exit|quit|q]<enter>    : Shutdown the  exchange engine                            #");
        System.out.println("      #  [engine|e] [OPTIONS]    : engine options                                           #");
        System.out.println("      #      [<|-f|--filename] [filepath]<enter> : E.g file1.txt, ./subdir/file2.txt        #");
        System.out.println("      #      [-o|--orders] : display / print latest order book status                       #");
        System.out.println("      #      [-t|--trades] : display / print all trades                                     #");
        System.out.println("      #      [-d|--delete] : delete all data (orders/trades) (to re-feed in test env)       #");
        System.out.println("      #  [cat|type] [file] | [engine|e]<enter>:feed order from csv  file                    #");
        System.out.println("      #  echo [line] | [engine|e]<enter> : feed order as csv raw text                       #");
        System.out.println("      #                                                                                     #");
        System.out.println("      #######################################################################################");
    }

    private static class Holder {
        private static final CmdHelp INSTANCE = new CmdHelp();
    }
}
