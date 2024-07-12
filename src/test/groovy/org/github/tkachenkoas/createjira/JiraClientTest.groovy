package org.github.tkachenkoas.createjira


import org.junit.jupiter.api.Test
import org.mockserver.integration.ClientAndServer
import org.mockserver.junit.jupiter.MockServerSettings
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.mockserver.model.JsonBody.json

@MockServerSettings
class JiraClientTest {
    private final ClientAndServer mockServer

    JiraClientTest(ClientAndServer mockServer) {
        this.mockServer = mockServer
    }

    @Test
    void testGetAvailableProjects() {
        mockServer.when(
                HttpRequest.request()
                        .withMethod("GET")
                        .withPath("/rest/api/3/project")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withBody(json([
                                [key: "PROJ"],
                                [key: "TEST"]
                        ]))
        )

        def clientParams = new ClientParams(
                jiraUrl: "http://localhost:${mockServer.getPort()}",
                jiraApiToken: 'test-token',
                githubToken: '',
                githubApiUrl: ''
        )

        def projects = JiraClient.getAvailableProjects(clientParams)

        assertEquals(['PROJ', 'TEST'], projects)
    }

    @Test
    void testCreateJiraTicket() {
        mockServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath("/rest/api/3/issue")
        ).respond(
                HttpResponse.response()
                        .withStatusCode(201)
                        .withBody(json([key: "PROJ-123"]))
        )

        def clientParams = new ClientParams(
                jiraUrl: "http://localhost:${mockServer.getPort()}",
                jiraApiToken: 'test-token',
                githubToken: '',
                githubApiUrl: ''
        )

        def issueKey = JiraClient.createJiraTicket(clientParams, 'PROJ', 'Test Title', 'Test Description', 'test-user')

        assertEquals('PROJ-123', issueKey)
    }
}
