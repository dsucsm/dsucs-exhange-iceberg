package org.dsucs.console;

import org.dsucs.Util;
import org.dsucs.engine.*;

import java.util.List;
import java.util.Scanner;

import static org.dsucs.Util.isNullEmpty;

public class CmdEngine extends Cmd {
    OrderProcessor processor = new OrderProcessor();
    OrderBook book = OrderBookImpl.getInstance();

    private CmdEngine() {
    }

    public static CmdEngine getInstance() {
        return CmdEngine.Holder.INSTANCE;
    }

    @Override
    public void execute() {
        String cmdLine = this.getCmdLine();
        boolean isEngine = cmdLine.matches(Util.cmdEnginePrefix) || cmdLine.matches(Util.cmdEngineSuffix);
        if (isEngine) {
            if (cmdLine.contains("<")) {
                cmdLine = cmdLine.split("<")[1].trim();
            } else if (cmdLine.contains("|")) {
                cmdLine = cmdLine.split("\\|")[0].trim();
            }

            if (cmdLine.startsWith("cat")) {
                cmdLine = cmdLine.substring(3).trim();
            } else if (cmdLine.startsWith("echo") || cmdLine.startsWith("type")) {
                cmdLine = cmdLine.substring(4).trim();
            }
        }
        if (cmdLine.matches(Util.orderLineFormat)) {
            processor.processOrderLine(cmdLine);
            PrintOption opt = PrintOption.COMPACT;
            if (!isNullEmpty(System.getProperty("printOption")) && System.getProperty("printOption").toUpperCase().matches(PrintOption.COMPREHENSIVE.name())) {
                opt = PrintOption.COMPREHENSIVE;
            }
            book.printOrders(opt);
        } else if (Util.isFilePathValid(cmdLine)) {
            processor.processOrderFile(cmdLine);
        } else if (isEngine) {
            try (Scanner scanner = new Scanner(this.getCmdLine())) {
                List<String> tokens = scanner.tokens().toList();
                switch (tokens.get(1)) {
                    case "-o", "--orders":
                        book.printOrders(PrintOption.COMPREHENSIVE);
                        break;
                    case "-t", "--trades":
                        book.printTrades(PrintOption.COMPREHENSIVE);
                        break;
                    case "-d", "--delete":
                        if (Util.askYesNo("CAUTION !!!\n" +
                                "All live data (Orders + Matches ) will be deleted from the exchange.\n" +
                                "Are you sure to delete all data ? (Y/N): ")) {
                            System.out.println("Proceeding with the deletion...");
                            book.deleteAll();
                            System.out.println("All live data deleted.");
                        } else {
                            System.out.println("Delete operation cancelled.");
                        }
                        break;
                    case "<", "-f", "--filename":
                        processor.processOrderFile(tokens.getLast());
                        break;
                }
            }
        }
    }


    private static class Holder {
        private static final CmdEngine INSTANCE = new CmdEngine();
    }
}
