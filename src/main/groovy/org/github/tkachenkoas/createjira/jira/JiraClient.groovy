package org.github.tkachenkoas.createjira.jira


import groovy.json.JsonSlurper
import org.github.tkachenkoas.createjira.ClientParams

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class JiraClient {
    static def getAvailableProjects(
            ClientParams clientParams
    ) {
        def jiraAuth = getAuthHeader(clientParams)
        def client = HttpClient.newHttpClient()
        def request = HttpRequest.newBuilder()
                .uri(URI.create("${clientParams.jiraUrl}/rest/api/3/project"))
                .header("Authorization", jiraAuth)
                .header("Content-Type", "application/json")
                .GET()
                .build()

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 200) {
            def projects = new JsonSlurper().parseText(response.body())
            return projects.collect { it.key }
        } else {
            println "Failed to fetch JIRA projects: ${response.body()}"
            return []
        }
    }

    private static String getAuthHeader(ClientParams clientParams) {
        return "Basic " + Base64.getEncoder().encodeToString(
                ("${clientParams.jiraApiUser}:${clientParams.jiraApiToken}").getBytes()
        )
    }

    static def createJiraTicket(
            ClientParams clientParams, String project,
            String title, String descriptionJson
    ) {
        def client = HttpClient.newHttpClient()

        def body = """
            {
                "fields": {
                    "project": { "key": "${project}" },
                    "summary": "${title}",
                    "description": ${descriptionJson},
                    "issuetype": { "name": "Task" }                    
                }
            }
            """

        def request = HttpRequest.newBuilder()
                .uri(URI.create("${clientParams.jiraUrl}/rest/api/3/issue"))
                .header("Authorization", getAuthHeader(clientParams))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()

        println("Sending create jira request: ${body}")

        def response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 201) {
            def responseBody = new JsonSlurper().parseText(response.body())
            return responseBody.key
        } else {
            println "Failed to create JIRA ticket: ${response.body()}"
            return null
        }
    }
}
