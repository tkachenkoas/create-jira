package org.github.tkachenkoas.createjira

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull

class CommandParserTest {
    @Test
    void testParseCommandWithAllParameters() {
        String command = 'project="PROJ" title="Fix login issue" description="Investigate and fix the login issue that causes a 500 error."'
        CommandParams params = CommandParser.parseCommand(command)

        assertEquals('PROJ', params.project)
        assertEquals('Fix login issue', params.title)
        assertEquals('Investigate and fix the login issue that causes a 500 error.', params.description)
    }

    @Test
    void testParseCommandWithTitleOnly() {
        String command = 'title="Fix login issue"'
        CommandParams params = CommandParser.parseCommand(command)

        assertNull(params.project)
        assertEquals('Fix login issue', params.title)
        assertNull(params.description)
    }

    @Test
    void testParseCommandWithDescriptionOnly() {
        String command = 'description="Investigate and fix the login issue that causes a 500 error."'
        CommandParams params = CommandParser.parseCommand(command)

        assertNull(params.project)
        assertNull(params.title)
        assertEquals('Investigate and fix the login issue that causes a 500 error.', params.description)
    }

    @Test
    void testParseCommandWithEmptyCommand() {
        String command = ''
        CommandParams params = CommandParser.parseCommand(command)

        assertNull(params.project)
        assertNull(params.title)
        assertNull(params.description)
    }

    @Test
    void testParseCommandWithInvalidFormat() {
        String command = 'project=PROJ title=Fix login issue description=Investigate and fix the login issue'
        CommandParams params = CommandParser.parseCommand(command)

        assertNull(params.project)
        assertNull(params.title)
        assertNull(params.description)
    }

    @Test
    void testParseCommandWithSpecialCharacters() {
        String command = 'project="PROJ" title="Fix login issue @#$%^&*()" description="Investigate and fix the login issue with special characters @#$%^&*()."'
        CommandParams params = CommandParser.parseCommand(command)

        assertEquals('PROJ', params.project)
        assertEquals('Fix login issue @#$%^&*()', params.title)
        assertEquals('Investigate and fix the login issue with special characters @#$%^&*().', params.description)
    }
}
