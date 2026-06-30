package org.dsucs.console;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class CmdExitTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        // Redirect System.out to capture console output
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Verify CmdExit is a Singleton")
    void testSingletonInstance() {
        CmdExit instance1 = CmdExit.getInstance();
        CmdExit instance2 = CmdExit.getInstance();

        // Assert that both references point to the exact same object
        assertSame(instance1, instance2, "CmdExit should return the same instance");
    }

    @Test
    @DisplayName("Verify isExit returns true")
    void testIsExit() {
        assertTrue(CmdExit.getInstance().isExit(), "CmdExit must signal loop termination");
    }

    @Test
    @DisplayName("Verify execute() prints shutdown message")
    void testExecuteOutput() {
        CmdExit.getInstance().execute();
        String output = outContent.toString().trim();
        assertEquals("Shutting down ORDER MATCHING ENGINE...", output, "Output message mismatch");
    }
}
