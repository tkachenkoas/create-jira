package org.github.tkachenkoas.createjira

class ClientParams {
    String jiraUrl
    String jiraApiToken
    String jiraApiUser
    String githubToken
    String githubApiUrl

    void validateInputs() {
        boolean isInvalid = false;
        if (!jiraUrl) {
            println("Jira URL is not set")
            isInvalid = true
        }
        if (!jiraApiToken) {
            println("Jira API token is not set")
            isInvalid = true
        }
        if (!jiraApiUser) {
            println("Jira API user is not set")
            isInvalid = true
        }
        if (!githubToken) {
            println("GitHub token is not set")
            isInvalid = true
        }
        if (!githubApiUrl) {
            println("GitHub API URL is not set")
            isInvalid = true
        }
        if (isInvalid) {
            throw new IllegalArgumentException("Invalid input parameters")
        }
    }
}
