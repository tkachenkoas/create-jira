package org.github.tkachenkoas.createjira

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull

class CommandParserTest {
    @Test
    void testParseCommandWithAllParameters() {
        String command = 'PROJ "Fix login issue" "Investigate and fix the login issue that causes a 500 error."'
        CommandParams params = CommandParser.parseCommand(command)

        assertEquals('PROJ', params.project)
        assertEquals('Fix login issue', params.title)
        assertEquals('Investigate and fix the login issue that causes a 500 error.', params.description)
    }

    @Test
    void testParseCommandWithTitleOnly() {
        String command = 'PROJ "Fix login issue"'
        CommandParams params = CommandParser.parseCommand(command)

        assertEquals('PROJ', params.project)
        assertEquals('Fix login issue', params.title)
        assertNull(params.description)
    }

}
