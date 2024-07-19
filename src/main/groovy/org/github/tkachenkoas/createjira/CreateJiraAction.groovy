package org.github.tkachenkoas.createjira


import com.fasterxml.jackson.databind.ObjectMapper
import org.github.tkachenkoas.createjira.jira.DescriptionJsonUtil
import org.github.tkachenkoas.createjira.jira.JiraClient

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES

class CreateJiraAction {

    static void main(String[] args) {
        def contextJson = getEnvOrProp('GITHUB_CONTEXT')

        println("Context JSON: ${contextJson}")

        GitHubContext gitHubContext = new ObjectMapper()
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(contextJson, GitHubContext.class)

        def clientParams = new ClientParams(
                jiraUrl: getEnvOrProp('INPUT_JIRA_URL'),
                jiraApiToken: getEnvOrProp('INPUT_JIRA_API_TOKEN'),
                jiraApiUser: getEnvOrProp('INPUT_JIRA_USER'),
                githubToken: gitHubContext.token,
                githubApiUrl: getEnvOrProp('GITHUB_API_URL')
        )
        def mockServerUrl = getEnvOrProp('MOCK_SERVER_URL')
        if (mockServerUrl) {
            clientParams.jiraUrl = mockServerUrl
            clientParams.githubApiUrl = mockServerUrl
        }

        println("Client params: ${new ObjectMapper().writeValueAsString(clientParams)}")

        handleCreateJiraCommand(gitHubContext, clientParams)
    }

    static void handleCreateJiraCommand(
            GitHubContext gitHubContext,
            ClientParams clientParams
    ) {
        String command = '/create-jira'
        def comment = gitHubContext.event.comment.body

        if (!comment.startsWith(command)) {
            println 'No command found in the comment, skipping'
            return
        }

        def commentUrl = gitHubContext.event.comment.html_url

        CommandParams commandParams = CommandParser.parseCommand(comment.substring(command.length()).trim())
        def project = commandParams.project
        if (!project) {
            println 'No project specified and could not determine project from context'
            return
        }
        def title = commandParams.title ?: 'GRTKT: Task from Pull Request comments'
        def issue = gitHubContext.event.issue
        def prUrl = issue.pull_request.html_url

        def descriptionJson = DescriptionJsonUtil.createDescriptionJson(
                commandParams.description,
                prUrl,
                commentUrl
        )

        def issueKey = JiraClient.createJiraTicket(
                clientParams, project,
                title, descriptionJson
        )
        if (issueKey) {
            GitHubClient.addComment(
                    clientParams,
                    issue.comments_url,
                    issueKey
            )
        }
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
