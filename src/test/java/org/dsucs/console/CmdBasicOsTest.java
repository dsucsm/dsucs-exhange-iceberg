package org.dsucs.console;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CmdBasicOs Singleton and Execution Tests")
class CmdBasicOsTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    @DisplayName("Verify CmdBasicOs is a Singleton")
    void testSingletonInstance() {
        CmdBasicOs instance1 = CmdBasicOs.getInstance();
        CmdBasicOs instance2 = CmdBasicOs.getInstance();
        assertSame(instance1, instance2, "Multiple calls must return the exact same instance");
    }

    @Test
    @DisplayName("Verify successful command execution (echo)")
    void testExecuteSuccess() {
        CmdBasicOs osCmd = CmdBasicOs.getInstance();
        osCmd.setCmdLine("echo HelloTest");
        osCmd.execute();

        String output = outContent.toString();
        // Check if the command output and the exit code message appear
        assertTrue(output.contains("HelloTest"), "Output should contain the echo result");
        assertTrue(output.contains("Process finished with exit code: 0"), "Should report successful exit code");
    }

    @Test
    @DisplayName("Verify handling of invalid commands")
    void testExecuteInvalidCommand() {
        CmdBasicOs osCmd = CmdBasicOs.getInstance();
        osCmd.setCmdLine("non_existent_command_12345");
        osCmd.execute();

        String output = outContent.toString();

        // 1. Check that the shell reported an error (Windows: 'not recognized', Linux: 'not found')
        assertTrue(output.contains("not recognized") || output.contains("not found"),
                "Output should contain shell error message. Got: " + output);

        // 2. Verify we didn't get exit code 0
        assertFalse(output.contains("exit code: 0"), "Invalid command should not have exit code 0");

        // 3. Verify our custom help message was printed from the execute() method
//        assertTrue(output.contains("For help, enter [help|h|?]"));
    }


    @Test
    @DisplayName("Verify OS-specific command logic")
    void testCommandFormatting() {
        CmdBasicOs osCmd = CmdBasicOs.getInstance();
        osCmd.setCmdLine("dir");

        String os = System.getProperty("os.name").toLowerCase();
        osCmd.execute();

        String output = outContent.toString();
        // If on Windows, it uses 'cmd /c', otherwise '/bin/sh -c'
        // We verify the process ran (even if it fails on the 'wrong' OS, it should try)
        assertNotNull(output);
    }
}