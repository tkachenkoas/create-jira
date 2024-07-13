package org.github.tkachenkoas.createjira

class CommandParser {

    static CommandParams parseCommand(String command) {
        def params = new CommandParams()
        def regex = /(\w+)\s*"([^"]+)"\s*"([^"]*)"?/
        def matcher = command =~ regex
        if (matcher.find()) {
            params.project = matcher.group(1)
            params.title = matcher.group(2)
            params.description = matcher.group(3)
        } else {
            regex = /(\w+)\s*"([^"]+)"/
            matcher = command =~ regex
            if (matcher.find()) {
                params.project = matcher.group(1)
                params.title = matcher.group(2)
            }
        }
        return params
    }

}
