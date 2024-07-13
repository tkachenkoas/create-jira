package org.github.tkachenkoas.createjira


import com.fasterxml.jackson.databind.ObjectMapper

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES

class CreateJiraAction {

    static void main(String[] args) {
        // GITHUB_EVENT_PATH
        String eventPath = getEnvOrProp('GITHUB_EVENT_PATH')
        String contextJson = new File(eventPath).text
        println "Event JSON: ${contextJson}"

        GitHubContext gitHubContext = new ObjectMapper()
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(contextJson, GitHubContext.class)

        def clientParams = new ClientParams(
                jiraUrl: getEnvOrProp('INPUT_JIRA_URL'),
                jiraApiToken: getEnvOrProp('INPUT_JIRA_API_TOKEN'),
                githubToken: getEnvOrProp('INPUT_GITHUB_TOKEN'),
                githubApiUrl: getEnvOrProp('GITHUB_API_URL') ?: "https://api.github.com"
        )

        println("Context: ${gitHubContext}")
        println("Client params: ${clientParams}")

        handleCreateJiraCommand(gitHubContext, clientParams)
    }

    static void handleCreateJiraCommand(
            GitHubContext gitHubContext,
            ClientParams clientParams
    ) {
        String command = '/create-jira'
        def comment = gitHubContext.payload.comment.body

        if (!comment.startsWith(command)) {
            println 'No command found in the comment, skipping'
            return
        }

        def commentAuthor = gitHubContext.payload.comment.user.login
        def commentUrl = gitHubContext.payload.comment.html_url
        def issue = gitHubContext.issue
        def prUrl = issue.html_url
        def prTitle = issue.title
        def branchName = gitHubContext.pull_request?.head?.ref

        CommandParams commandParams = CommandParser.parseCommand(comment.substring(command.length()).trim())
        def project = commandParams.project ?: determineProject(prTitle, branchName, clientParams)
        if (!project) {
            println 'No project specified and could not determine project from context'
            return
        }
        def title = commandParams.title ?: 'GRTKT: Task from Pull Request comments'
        def description = commandParams.description ?: ''

        description += """\n\n**Context:**\n\nThis JIRA ticket was created from a GitHub Pull Request.\n\n- **Pull Request:** [#${issue.number}](${prUrl})\n- **Comment:** [View comment](${commentUrl})\n- **Author:** @${commentAuthor}"""

        def issueKey = JiraClient.createJiraTicket(clientParams, project, title, description, commentAuthor)
        if (issueKey) {
            GitHubClient.addComment(clientParams, gitHubContext.repo.owner, gitHubContext.repo.repo, issue.number, issueKey)
        }
    }

    static def determineProject(String prTitle, String branchName, ClientParams clientParams) {
        def availableProjects = JiraClient.getAvailableProjects(clientParams)
        def result = availableProjects.find {
            prTitle?.contains(it) || branchName?.contains(it)
        }
        println "Resolved project from branch/pr: ${result}"
        return result
    }

    static def getEnvOrProp(
            String name
    ) {
        def value = System.getenv(name)
        if (value == null) {
            value = System.getProperty(name)
        }
        return value
    }

}
