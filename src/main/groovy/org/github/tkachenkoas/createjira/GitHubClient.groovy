package org.github.tkachenkoas.createjira

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class GitHubClient {
    static void addComment(ClientParams clientParams, String owner, String repo, int issueNumber, String issueKey) {
        def client = HttpClient.newHttpClient()
        def request = HttpRequest.newBuilder()
                .uri(URI.create("${clientParams.githubApiUrl}/repos/${owner}/${repo}/issues/${issueNumber}/comments"))
                .header("Authorization", "token ${clientParams.githubToken}")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
            {
                "body": "JIRA ticket created: [${issueKey}](${clientParams.jiraUrl}/browse/${issueKey})"
            }
            """))
                .build()

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 201) {
            println "Failed to add GitHub comment: ${response.body()}"
        } else {
            println "JIRA ticket ${issueKey} created successfully"
        }
    }
}
