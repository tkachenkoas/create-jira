package org.github.tkachenkoas.createjira

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class GitHubClient {
    static void addComment(
            ClientParams clientParams,
            String commentsUrl,
            String jiraIssueKey
    ) {
        def client = HttpClient.newHttpClient()
        def request = HttpRequest.newBuilder()
                .uri(URI.create(
                        commentsUrl.replace("https://api.github.com", clientParams.githubApiUrl)
                ))
                .header("Authorization", "Bearer " + clientParams.githubToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
            {
                "body": "JIRA ticket created: [${jiraIssueKey}](${clientParams.jiraUrl}/browse/${jiraIssueKey})"
            }
            """))
                .build()

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 201) {
            println "Failed to add GitHub comment: ${response.body()}"
        } else {
            println "JIRA ticket ${jiraIssueKey} created successfully"
        }
    }
}
