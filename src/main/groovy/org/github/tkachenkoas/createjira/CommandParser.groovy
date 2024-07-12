package org.github.tkachenkoas.createjira

import java.util.regex.Pattern

class CommandParser {

    static CommandParams parseCommand(String command) {
        def params = new CommandParams()
        def regex = Pattern.compile(/(\w+)=["']([^"']+)["']/)
        def matcher = command =~ regex
        matcher.each { match ->
            params[match[1]] = match[2]
        }
        return params
    }

}
