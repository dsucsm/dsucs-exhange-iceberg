package org.dsucs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Util {
    private static Scanner scanner; // keep it open
    public final static String cmdHelp = "help|h|\\?";
    public final static String cmdExit = "exit|quit|q";
    public final static String cmdEnginePrefix = "^(engine|e)\\s+.*";
    public final static String cmdEngineSuffix = ".*\\s+(e|engine)$";
    public final static String orderLineFormat = "^.{1,12},[SsBb](,\\d{1,9}){2,3}$";
    public final static String nonWhiteSpace = "[\\S]*";

    public static boolean isNullEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isFilePathValid(String file){
        try{
            return Files.exists(Paths.get(file));
        }catch(Exception e){
            System.err.println("Invalid file path :" + file);
        }
        return false;
    }

    public static boolean askYesNo(String question) {
        String answer;
        if (scanner == null) {
            scanner = new Scanner(System.in);
        }
        do {
            System.out.print(question);
            answer = scanner.next().trim().toUpperCase();
            // Loop until the answer matches "Y" or "N"
        } while (!answer.matches("[Y]") && !answer.matches("[N]"));
        return answer.equals("Y");
    }
}
